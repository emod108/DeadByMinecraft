package me.mod108.deadbyminecraft.targets.props;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.utility.Directions;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Generator extends Prop {
    public enum GeneratorType { INDOOR, OUTDOOR }
    public enum GeneratorState { IDLE, REGRESSING, REPAIRED }

    // Generator's side. Only by standing on these sides you can repair/break generators
    public static class GeneratorSide {
        // Block which belong to this side
        private final ArrayList<Location> blocks = new ArrayList<>();

        // Which side it is
        private final BlockFace facing;

        // Player who currently occupies this side
        private Character player = null;

        // If you can interact with generator from this side
        private boolean obstructed = false;

        public GeneratorSide(final BlockFace facing) {
            this.facing = facing;
        }

        public ArrayList<Location> getBlocks() {
            return blocks;
        }

        public void addBlock(final Block block) {
            blocks.add(block.getLocation());
            if (block.getType() != Material.AIR)
                obstructed = true;
        }

        public BlockFace getFacing() {
            return facing;
        }

        public Character getPlayer() {
            return player;
        }

        public void setPlayer(final Character player) {
            this.player = player;
        }

        boolean isObstructed() {
            return obstructed;
        }
    }

    // Materials
    public static final Material INDOOR_MATERIAL = Material.YELLOW_TERRACOTTA;
    public static final Material OUTDOOR_MATERIAL = Material.RED_TERRACOTTA;
    public static final Material FRONT_UPPER_MATERIAL = Material.CYAN_TERRACOTTA;
    public static final Material BACK_UPPER_MATERIAL = Material.SMOOTH_STONE_SLAB;
    public static final Material PILLAR_MATERIAL = Material.OAK_FENCE;
    public static final Material NOT_REPAIRED_LAMP_MATERIAL = Material.REDSTONE_LAMP;
    public static final Material REPAIRED_LAMP_MATERIAL = Material.GLOWSTONE;

    public static final Sound REPAIR_SOUND = Sound.UI_BUTTON_CLICK;
    public static final Sound FINISH_REPAIR_SOUND = Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST;

    // How many fences the generator's lamp sits on
    public static final int INDOOR_LAMP_HEIGHT = 1;
    public static final int OUTDOOR_LAMP_HEIGHT = 4;

    // Max repair progress
    public static final float MAX_REPAIR_PROGRESS = 10f;

    // Current repair progress
    private float repairProgress = 0f;

    // A sound of generator having repair progress is being constantly played
    final BukkitRunnable idleProgressSound = new BukkitRunnable() {
        static final int MAX_TIME_BEFORE_SOUND = Timings.secondsToTicks(2);
        static final int MIN_TIME_BEFORE_SOUND = Timings.secondsToTicks(0.05);
        static final Sound IDLE_SOUND_FIRST = Sound.BLOCK_PISTON_EXTEND;
        static final Sound IDLE_SOUND_SECOND = Sound.BLOCK_PISTON_CONTRACT;

        int ticksRan = 0;
        Sound currentIdleSound = IDLE_SOUND_FIRST;

        @Override
        public void run() {
            if (repairProgress == 0f)
                return;

            // Idle sound if repaired
            if (generatorState == GeneratorState.REPAIRED) {
                SoundManager.playForAll(location, currentIdleSound, 0.04f, 2.0f);
                currentIdleSound = (currentIdleSound == IDLE_SOUND_FIRST) ? IDLE_SOUND_SECOND : IDLE_SOUND_FIRST;
                return;
            }

            final float progressPercents = getProgressPercents();
            final int ticksBeforeSound = (int) (MAX_TIME_BEFORE_SOUND - (progressPercents *
                    (MAX_TIME_BEFORE_SOUND - MIN_TIME_BEFORE_SOUND)));

            ++ticksRan;
            if (ticksRan > ticksBeforeSound) {
                ticksRan = 0;
                SoundManager.playForAll(location, currentIdleSound, progressPercents, 2.0f);
                currentIdleSound = (currentIdleSound == IDLE_SOUND_FIRST) ? IDLE_SOUND_SECOND : IDLE_SOUND_FIRST;
            }
        }
    };

    // Generator type
    private final GeneratorType generatorType;

    // Generator state
    private GeneratorState generatorState = GeneratorState.IDLE;

    // Generator sides
    private final ArrayList<GeneratorSide> generatorSides = new ArrayList<>();

    // Blocks, player can interact with to interact with the generator
    private final ArrayList<Block> interactableBlocks = new ArrayList<>();

    public Generator(final Location location, final BlockFace direction, final GeneratorType generatorType) {
        super(location, direction);
        this.generatorType = generatorType;
    }

    @Override
    public void build() {
        final Location currentLocation = location.clone();

        final Material bottomMaterial = (generatorType == GeneratorType.OUTDOOR) ? OUTDOOR_MATERIAL : INDOOR_MATERIAL;
        final int lampHeight = (generatorType == GeneratorType.OUTDOOR) ? OUTDOOR_LAMP_HEIGHT : INDOOR_LAMP_HEIGHT;

        // Building the front half
        interactableBlocks.add(placeBlock(currentLocation, bottomMaterial));
        interactableBlocks.add(placeBlock(currentLocation.add(0, 1, 0), FRONT_UPPER_MATERIAL));
        for (int i = 0; i < lampHeight; ++i) {
            placeBlock(currentLocation.add(0, 1, 0), PILLAR_MATERIAL);
        }
        placeBlock(currentLocation.add(0, 1, 0), NOT_REPAIRED_LAMP_MATERIAL);

        // Building the back half
        // Lowering back to the starting location
        currentLocation.add(0, -(2 + lampHeight), 0);

        // Choosing were to place blocks
        currentLocation.add(Directions.getVector(direction, 1));
        interactableBlocks.add(placeBlock(currentLocation, bottomMaterial));
        interactableBlocks.add(placeBlock(currentLocation.add(0, 1, 0), BACK_UPPER_MATERIAL));

        // Calculating sides
        calculateSides();

        // Generator sounds
        idleProgressSound.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    @Override
    public void destroy() {
        idleProgressSound.cancel();

        // Make it so everyone who interacts with this generator stops
        for (final Character player : getInteractingPlayers()) {
            if (player == null)
                continue;
            player.cancelAction();
        }
        super.destroy();
    }

    private void calculateSides() {
        // Getting front and back
        final Location frontBlock = location.clone();
        final Location backBlock = frontBlock.clone();
        backBlock.add(Directions.getVector(direction, 1));

        // Calculating sides
        generatorSides.clear();
        calculateSide(frontBlock, backBlock, BlockFace.NORTH, new Vector(0, 0, -1));
        calculateSide(frontBlock, backBlock, BlockFace.SOUTH, new Vector(0, 0, 1));
        calculateSide(frontBlock, backBlock, BlockFace.EAST, new Vector(1, 0, 0));
        calculateSide(frontBlock, backBlock, BlockFace.WEST, new Vector(-1, 0, 0));
    }

    private void calculateSide(final Location frontBlock, final Location backBlock,
                               final BlockFace facing, final Vector vector) {
        // Creating side
        final GeneratorSide side = new GeneratorSide(facing);

        // Calculating side
        frontBlock.add(vector);
        if (!frontBlock.equals(backBlock))
            side.addBlock(frontBlock.getBlock());
        frontBlock.subtract(vector);

        backBlock.add(vector);
        if (!backBlock.equals(frontBlock))
            side.addBlock(backBlock.getBlock());
        backBlock.subtract(vector);

        // Adding side
        generatorSides.add(side);
    }

    // Converts percents to progress
    public static float percentsToProgress(final float percents) {
        return percents * MAX_REPAIR_PROGRESS;
    }

    // Converts repair progress to percents
    public static float progressToPercents(final float progress) {
        return progress / MAX_REPAIR_PROGRESS;
    }

    // Returns current repair progress in range from 0.0 to 1.0
    public float getProgressPercents() {
        return progressToPercents(repairProgress);
    }

    // Adds this progress to generator repair
    // If it's below 0, nothing happens
    // If progress after this operation reaches max progress, generator becomes repaired
    public void addRepairProgress(final float progress) {
        if (progress <= 0.0f)
            return;

        repairProgress += progress;

        // Repairing was finished
        if (repairProgress >= MAX_REPAIR_PROGRESS)
            becomeRepaired();
    }

    // This function is called when all generators are fully repaired
    public void becomeRepaired() {
        generatorState = GeneratorState.REPAIRED;
        for (final Block block : blocks) {
            if (block.getType() == NOT_REPAIRED_LAMP_MATERIAL)
                block.setType(REPAIRED_LAMP_MATERIAL);
        }

        // Make it so everyone who interacts with this generator stops
        for (final Character player : getInteractingPlayers()) {
            if (player == null)
                continue;

            player.cancelAction();
            player.getPlayer().sendMessage(ChatColor.GREEN + "Generator has been repaired!");
        }

        SoundManager.playForAll(location, FINISH_REPAIR_SOUND, 100f, 1f);
    }

    // Decreases repair progress
    public void regress(final float progress) {
        if (progress <= 0.0f)
            return;

        repairProgress -= progress;

        // Progress can't go below 0
        if (repairProgress < 0.0f)
            repairProgress = 0.0f;
    }

    public GeneratorType getGeneratorType() {
        return generatorType;
    }

    public GeneratorState getGeneratorState() {
        return generatorState;
    }

    // Returns generator side
    public GeneratorSide getGeneratorSide(final BlockFace generatorSide) {
        for (final GeneratorSide side : generatorSides)
            if (side.getFacing() == generatorSide)
                return side;

        return null;
    }

    public ArrayList<GeneratorSide> getGeneratorSides() {
        return generatorSides;
    }

    // Returns true if it's not possible to interact with generator from this side
    public boolean isObstructed(final BlockFace generatorSide) {
        for (final GeneratorSide side : generatorSides)
            if (side.getFacing() == generatorSide)
                return side.isObstructed();

        return true;
    }

    public void setInteractingPlayer(final Character player, final BlockFace generatorSide) {
        for (final GeneratorSide side : generatorSides)
            if (side.getFacing() == generatorSide)
                side.setPlayer(player);
    }

    public Character getInteractingPlayer(final BlockFace generatorSide) {
        for (final GeneratorSide side : generatorSides)
            if (side.getFacing() == generatorSide)
                return side.getPlayer();

        return null;
    }

    // Returns list of all players who interact with this generator
    public ArrayList<Character> getInteractingPlayers() {
        final ArrayList<Character> players = new ArrayList<>();
        for (final GeneratorSide side : generatorSides)
            players.add(side.getPlayer());

        return players;
    }

    public ArrayList<Block> getInteractableBlocks() {
        return interactableBlocks;
    }
}

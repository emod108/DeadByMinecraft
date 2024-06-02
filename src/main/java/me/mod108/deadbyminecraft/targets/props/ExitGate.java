package me.mod108.deadbyminecraft.targets.props;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.utility.Directions;
import me.mod108.deadbyminecraft.utility.EscapeLine;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("ExitGate")
public class ExitGate extends Prop {
    // Gate's parameters
    public static final Material GATE_SWITCH_MATERIAL = Material.LEVER;
    public static final Material LAMP_OFF_MATERIAL = Material.REDSTONE_LAMP;
    public static final Material LAMP_ON_MATERIAL = Material.SHROOMLIGHT;
    public static final Material GATE_MATERIAL = Material.IRON_BLOCK;
    public static final int GATES_WIDTH = 6;
    public static final int GATES_HEIGHT = 5;
    public static final float MAX_OPEN_PROGRESS = 10.0f;

    // How far is escape zone from lever's location
    private static final int ESCAPE_ZONE_DISTANCE = 10;

    // Enum which defines possible exit gate states
    public enum ExitGateState { UNPOWERED, POWERED, OPEN }

    // Lever, which opens the gate
    private Block gateSwitch;

    // Lamps, which turn on one after another
    private final Block[] lamps = new Block[3];

    // Exit gates can be opened only when active
    private ExitGateState gateState = ExitGateState.POWERED;

    // Current open progress
    private float openProgress = 0f;

    public ExitGate(final Location location, final BlockFace direction) {
        super(location, direction);
    }

    @Override
    public void build() {
        final Location currentLocation = location.clone().add(0, 1, 0);

        // Placing the gate switch
        gateSwitch = placeBlock(currentLocation, GATE_SWITCH_MATERIAL);
        final Switch leverData = (Switch) gateSwitch.getBlockData();
        leverData.setFacing(direction.getOppositeFace());
        gateSwitch.setBlockData(leverData);

        // Placing lamps
        currentLocation.add(Directions.getVector(direction, 1));
        currentLocation.add(Directions.getVector(Directions.turnLeft(direction), 1));
        lamps[0] = placeBlock(currentLocation.add(0, 1, 0), LAMP_OFF_MATERIAL);
        lamps[1] = placeBlock(currentLocation.add(0, 1, 0), LAMP_OFF_MATERIAL);
        lamps[2] = placeBlock(currentLocation.add(0, 1, 0), LAMP_OFF_MATERIAL);

        // Placing gates
        final Vector horizontalVector = Directions.getVector(Directions.turnRight(direction), 1);
        currentLocation.add(horizontalVector);
        currentLocation.add(horizontalVector);
        for (int i = 0; i < GATES_WIDTH; ++i) {
            for (int j = 0; j < GATES_HEIGHT; ++j) {
                placeBlock(currentLocation, GATE_MATERIAL);
                currentLocation.add(0, -1, 0);
            }
            currentLocation.add(horizontalVector);
            currentLocation.add(0, GATES_HEIGHT, 0);
        }
    }

    public Block getGateSwitch() {
        return gateSwitch;
    }

    public ExitGateState getGateState() {
        return gateState;
    }

    public void setGateState(final ExitGateState gateState) {
        this.gateState = gateState;
    }

    // Converts open progress to percents
    public static float progressToPercents(final float progress) {
        return progress / MAX_OPEN_PROGRESS;
    }

    // Returns current open progress in range from 0.0 to 1.0
    public float getProgressPercents() {
        return progressToPercents(openProgress);
    }

    public void addOpenProgress(final float progress) {
        if (progress <= 0.0f)
            return;

        openProgress += progress;

        // Updating lamps to indicate current progress
        for (int i = 0; i < lamps.length; ++i) {
            // Checking if there's enough progress for a lamp to turn on
            final float lampPercents = 0.25f * (i + 1);
            if (getProgressPercents() < lampPercents)
                break;

            // Turning a lamp on if necessary
            if (lamps[i].getType() != LAMP_ON_MATERIAL)
                lamps[i].setType(LAMP_ON_MATERIAL);
        }

        // Gates can be open
        if (openProgress >= MAX_OPEN_PROGRESS)
            open();
    }

    public void open() {
        // Finishing player's action
        final Character player = interactingPlayer;
        if (player != null) {
            player.cancelAction();
            player.getPlayer().sendMessage(ChatColor.GREEN + "Exit gate has been opened!");
        }

        // Opening the exit gates
        gateState = ExitGateState.OPEN;
        final Vector horizontalVector = Directions.getVector(Directions.turnRight(direction), 1);
        final Location currentLocation = lamps[2].getLocation().clone();
        currentLocation.add(horizontalVector);
        currentLocation.add(horizontalVector);
        for (int i = 0; i < GATES_WIDTH; ++i) {
            for (int j = 0; j < GATES_HEIGHT; ++j) {
                removeBlock(currentLocation, true);
                currentLocation.add(0, -1, 0);
            }
            currentLocation.add(horizontalVector);
            currentLocation.add(0, GATES_HEIGHT, 0);
        }

        // Creating escape zone
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null) {
            System.out.println("Game is null while opening the gates!");
            return;
        }

        // Creating escape zone
        final Location escapePoint = location.clone();
        escapePoint.add(Directions.getVector(direction, ESCAPE_ZONE_DISTANCE));
        escapePoint.add(Directions.getVector(Directions.turnRight(direction), 1));
        final EscapeLine escapeLine = new EscapeLine(escapePoint, direction);
        game.addEscapeLine(escapeLine);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("location", location);
        map.put("direction", direction);
        return map;
    }

    public static ExitGate deserialize(Map<String, Object> map) {
        return new ExitGate((Location) map.get("location"), (BlockFace) map.get("direction"));
    }
}

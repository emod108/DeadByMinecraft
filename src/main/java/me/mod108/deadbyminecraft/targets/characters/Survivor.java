package me.mod108.deadbyminecraft.targets.characters;

import me.mod108.crawlingplugin.CrawlingPlugin;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.actions.HealAction;
import me.mod108.deadbyminecraft.actions.SurvivorOpenExitAction;
import me.mod108.deadbyminecraft.actions.SurvivorUnhookAction;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import me.mod108.deadbyminecraft.targets.props.Generator;
import me.mod108.deadbyminecraft.targets.props.Hook;
import me.mod108.deadbyminecraft.targets.props.Locker;
import me.mod108.deadbyminecraft.utility.SpeedModifier;
import me.mod108.deadbyminecraft.utility.Timings;
import me.mod108.deadbyminecraft.actions.RepairAction;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class Survivor extends Character {
    public enum HealthState { HEALTHY, INJURED, DEEP_WOUND, DYING, BEING_CARRIED, HOOKED, DEAD }

    // After achieving this hook stage survivor dies
    private static final int MAX_HOOK_STAGE = 3;

    // Default time survivor has before hook stage progression (in ticks)
    private static final int STARTING_HOOKED_HEALTH = Timings.secondsToTicks(75);

    // Default speed is 100%
    public static final float DEFAULT_SPEED = 1.0f;

    // For how long survivors vault a vaultable prop in ticks
    private static final int DEFAULT_VAULT_TIME = Timings.secondsToTicks(0.5);

    // Each N ticks, blood particles are created
    private static final int DEFAULT_TICKS_TILL_BLOOD_PARTICLES = Timings.secondsToTicks(0.25);

    // Current hook stage
    private int hookStage = 0;

    // Time survivor has before hook stage progression (in ticks)
    private int hookedHealth = STARTING_HOOKED_HEALTH;

    // Hook survivor is hooked on
    private Hook hookedOn = null;

    // How survivor is doing
    private HealthState healthState = HealthState.HEALTHY;

    // Progress to reach so survivor can regain a health state
    private static final float MAX_HEALING_PROGRESS = 16.0f;

    // Current healing/recovery progress
    private float healingProgress = 0.0f;

    private int ticksTillBloodParticles = DEFAULT_TICKS_TILL_BLOOD_PARTICLES;

    public Survivor(final Player player) {
        super(player, DEFAULT_SPEED, DEFAULT_VAULT_TIME);
    }

    public HealthState getHealthState() {
        return healthState;
    }

    public void setHealthState(final HealthState healthState) {
        this.healthState = healthState;
    }

    public void getHit() {
        // Interrupting any actions after receiving a hit
        interruptAction();
        healingProgress = 0.0f;

        // Creating hit effect
        player.playHurtAnimation(0);
        SoundManager.playForAll(player.getEyeLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);

        // Spawning particles
        final World world = Bukkit.getWorld("world");
        if (world != null) {
            final Location bloodLocation = player.getLocation().clone();
            bloodLocation.add(0, player.getHeight() / 2, 0);
            world.spawnParticle(Particle.BLOCK_CRACK, bloodLocation, 50, 0.3, 0.3, 0.3,
                    Material.REDSTONE_BLOCK.createBlockData());
        }

        // Cancelling any actions after getting hit
        player.sendMessage(ChatColor.RED + "You were hit by the killer!");

        if (healthState == HealthState.HEALTHY) {
            healthState = HealthState.INJURED;
            player.sendMessage(ChatColor.RED + "You are now injured.");

            final SpeedModifier onHitSprint = new SpeedModifier(0.65f, Timings.secondsToTicks(1.8),
                    "On-hit Sprint");
            addSpeedModifier(onHitSprint);
            return;
        }

        if (healthState == HealthState.INJURED || healthState == HealthState.DEEP_WOUND) {
            goToDyingState();
        }
    }

    public void goToDyingState() {
        healthState = HealthState.DYING;
        CrawlingPlugin.getPlugin().getCrawlingManager().startCrawling(player);
        player.sendMessage(ChatColor.RED + "You are now in dying state.");
    }

    // Converts percents to progress
    public static float healingPercentsToProgress(final float percents) {
        return percents * MAX_HEALING_PROGRESS;
    }

    // Converts healing progress to percents
    public static float healingProgressToPercents(final float progress) {
        return progress / MAX_HEALING_PROGRESS;
    }

    // Returns current healing progress in range from 0.0 to 1.0
    public float getHealingProgressPercents() {
        return healingProgressToPercents(healingProgress);
    }

    // Returns current healing progress
    public float getHealingProgress() {
        return healingProgress;
    }

    public void addHealingProgress(final float progress) {
        if (!isHealable() || progress < 0.0f)
            return;

        healingProgress += progress;
        if (healingProgress >= MAX_HEALING_PROGRESS)
            recoverHealthState();
    }

    public void recoverHealthState() {
        healingProgress = 0.0f;
        switch (healthState) {
            case INJURED -> {
                healthState = HealthState.HEALTHY;
                player.sendMessage(ChatColor.RED + "You are no longer injured.");
            }
            case DEEP_WOUND -> {
                healthState = HealthState.INJURED;
                player.sendMessage(ChatColor.RED + "You are no longer in deep wound.");
            }
            case DYING -> {
                healthState = HealthState.INJURED;
                CrawlingPlugin.getPlugin().getCrawlingManager().stopCrawling(player);
                player.sendMessage(ChatColor.RED + "You are no longer in dying state.");
            }
        }
    }

    // This method simulates bleeding
    public void bleed() {
        if (ticksTillBloodParticles > 0) {
            --ticksTillBloodParticles;
            return;
        }

        ticksTillBloodParticles = DEFAULT_TICKS_TILL_BLOOD_PARTICLES;
        final World world = Bukkit.getWorld("world");
        if (world != null) {
            world.spawnParticle(Particle.BLOCK_CRACK, getLocation(), 1, 0.1, 0.1, 0.1,
                    Material.REDSTONE_BLOCK.createBlockData());
        }
    }

    public void enterLocker(final Locker locker) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        // Hiding survivor from every player
        plugin.vanishManager.hide(player);

        // Teleporting survivor
        final Location lockerTop = locker.getLocation().clone().
                add(DeadByMinecraft.CENTER_ADJUSTMENT, 2, DeadByMinecraft.CENTER_ADJUSTMENT);

        // Making teleport smooth for camera
        final Location playerPitchAndYaw = player.getLocation();
        lockerTop.setPitch(playerPitchAndYaw.getPitch());
        lockerTop.setYaw(playerPitchAndYaw.getYaw());
        player.teleport(lockerTop);

        // Freezing survivor
        plugin.freezeManager.freeze(player);
        locker.setHidingSurvivor(this);
        movementState = MovementState.IN_LOCKER;
    }

    public void leaveLocker(final Locker locker) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        // Showing player
        plugin.vanishManager.show(player);

        // Teleporting to the door
        final Location exitLocation = locker.getBottomDoorBlock().getLocation().clone();

        // Making teleport smooth for camera
        final Location playerPitchAndYaw = player.getLocation();
        exitLocation.setPitch(playerPitchAndYaw.getPitch());
        exitLocation.setYaw(playerPitchAndYaw.getYaw());
        player.teleport(exitLocation.add(DeadByMinecraft.CENTER_ADJUSTMENT, 0, DeadByMinecraft.CENTER_ADJUSTMENT));

        // Allowing to move
        plugin.freezeManager.unFreeze(player);
        locker.setHidingSurvivor(null);
        movementState = MovementState.IDLE;
    }

    public void getHooked(final Hook hook) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        hook.hook(this);
        plugin.freezeManager.freeze(player);

        final Location teleportLocation = hook.getHook().getRelative(0, -2, 0).getLocation().clone();
        player.teleport(teleportLocation.add(DeadByMinecraft.CENTER_ADJUSTMENT, 0, DeadByMinecraft.CENTER_ADJUSTMENT));
        healthState = HealthState.HOOKED;
        hookedOn = hook;
    }

    public void getUnhooked(final Hook hook) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        hook.unHook();

        plugin.freezeManager.unFreeze(player);
        healthState = HealthState.INJURED;
        hookedOn = null;
    }

    // Returns hook on which survivor is hooked on
    public Hook getHook() {
        return hookedOn;
    }

    public void startRepairing(final Generator generator, final Generator.GeneratorSide side) {
        side.setPlayer(this);
        action = new RepairAction(this, generator, side);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
        player.sendMessage(ChatColor.GREEN + "You have started repairing this generator. Don't move!");
    }

    public void startHealing(final Survivor healingTarget) {
        action = new HealAction(this, healingTarget);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
        player.sendMessage(ChatColor.GREEN + "You have started healing " + ChatColor.YELLOW +
                healingTarget.getPlayer().getDisplayName());
        healingTarget.getPlayer().sendMessage(ChatColor.GREEN + "You are now being healed by " + ChatColor.YELLOW +
                player.getDisplayName());
    }

    public void startUnhooking(final Survivor survivor) {
        action = new SurvivorUnhookAction(this, survivor);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
        player.sendMessage(ChatColor.GREEN + "You have started unhooking. Don't move!");
    }

    @Override
    public void startOpening(final ExitGate exitGate) {
        exitGate.setInteractingPlayer(this);
        action = new SurvivorOpenExitAction(this, exitGate);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
        player.sendMessage(ChatColor.GREEN + "You have started opening the exit gates. Don't move!");
    }

    // Returns true if the survivor is hittable
    // It doesn't show if he is damageable
    public boolean isHittable() {
        return (healthState != HealthState.DYING && healthState != HealthState.BEING_CARRIED &&
                healthState != HealthState.DEAD && movementState != MovementState.IN_LOCKER);
    }

    // Returns true if survivor is neither healthy, injured, nor deep wounded
    public boolean isIncapacitated() {
        return (healthState != HealthState.HEALTHY && healthState != HealthState.INJURED &&
                healthState != HealthState.DEEP_WOUND);
    }

    // Returns true, if survivor can be healed
    // It works if survivor is injured and isn't interacting with anything
    public boolean isHealable() {
        if (healthState != HealthState.INJURED && healthState != HealthState.DEEP_WOUND &&
                healthState != HealthState.DYING)
            return false;

        if (movementState != MovementState.IDLE)
            return false;

        return action == null;
    }

    @Override
    public boolean canInteract() {
        if (isIncapacitated())
            return false;

        if (movementState != MovementState.IDLE)
            return false;

        return action == null;
    }

    @Override
    public boolean canInteractWithLocker() {
        if (isIncapacitated())
            return false;

        return action == null;
    }

    @Override
    public boolean canInteractWithWindow() {
        return canInteract();
    }

    @Override
    public boolean canInteractWithPallet() {
        return canInteract();
    }

    @Override
    public boolean canInteractWithGenerator() {
        return canInteract();
    }

    @Override
    public boolean canInteractWithExitGate() {
        return canInteract();
    }

    @Override
    public boolean canInteractWithSurvivor() {
        return canInteract();
    }
}

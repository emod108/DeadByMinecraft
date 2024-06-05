package me.mod108.deadbyminecraft.targets.characters;

import me.mod108.crawlingplugin.CrawlingPlugin;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.actions.*;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.props.*;
import me.mod108.deadbyminecraft.utility.Game;
import me.mod108.deadbyminecraft.utility.SpeedModifier;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Survivor extends Character {
    public enum HealthState { HEALTHY, INJURED, DEEP_WOUND, DYING, BEING_CARRIED, HOOKED,
        SACRIFICED, DEAD, DISCONNECTED, ESCAPED }

    public static String healthStateToStr(final HealthState healthState) {
        return switch (healthState) {
            case HEALTHY -> ChatColor.GREEN + "HEALTHY";
            case INJURED -> ChatColor.YELLOW + "INJURED";
            case DEEP_WOUND -> ChatColor.YELLOW + "DEEP WOUND";
            case DYING -> ChatColor.RED + "DYING";
            case BEING_CARRIED -> ChatColor.RED + "CARRIED";
            case HOOKED -> ChatColor.RED + "HOOKED";
            case SACRIFICED -> ChatColor.DARK_RED + "SACRIFICED";
            case DEAD -> ChatColor.DARK_RED + "DEAD";
            case DISCONNECTED -> ChatColor.DARK_RED + "DISCONNECTED";
            case ESCAPED -> ChatColor.DARK_GREEN + "ESCAPED";
        };
    }

    // At which distances terror radius plays
    private static final double TERROR_FAR_SQUARED = 32 * 32;
    private static final int TERROR_FAR_HEARTBEAT_DELAY = Timings.secondsToTicks(1.5);
    private static final double TERROR_CLOSER_SQUARED = 16 * 16;
    private static final int TERROR_CLOSER_HEARTBEAT_DELAY = Timings.secondsToTicks(1);
    private static final double TERROR_CLOSE_SQUARED = 8 * 8;
    private static final int TERROR_CLOSE_HEARTBEAT_DELAY = Timings.secondsToTicks(0.5);
    private static final int TERROR_SECOND_BEAT_DELAY = Timings.secondsToTicks(0.15);

    private int currentTerrorBeatDelay = 0;
    private int nextBeat = 0;
    private boolean secondBeat = false;

    // After achieving this hook stage survivor dies
    private static final int MAX_HOOK_STAGE = 3;

    // Default time survivor has before hook stage progression (in ticks)
    private static final int STAGE_PROGRESSION_TIME = Timings.secondsToTicks(60);

    // Penalty applied to stage progression time if self-unhook was unsuccessful
    private static final int FAILED_UNHOOK_TIME_PENALTY = Timings.secondsToTicks(20);

    // Chance of unhooking yourself
    private static final double SELF_UNHOOK_CHANCE = 0.04;

    // Default time survivor has before dying from bleeding out
    private static final int STARTING_BLEEDOUT_TIME = Timings.secondsToTicks(240);

    // Default speed is 100%
    public static final float DEFAULT_SPEED = 1.0f;

    // For how long survivors fast vault in ticks
    private static final int FAST_VAULT_TIME = Timings.secondsToTicks(0.5);

    // For how long survivors slow vault in ticks
    private static final int SLOW_VAULT_TIME = Timings.secondsToTicks(1.5);

    // Rushed locker enter / leave time
    private static final int RUSHED_LOCKER_TIME = Timings.secondsToTicks(0.5);

    // Slow locker enter / leave time
    private static final int SLOW_LOCKER_TIME = Timings.secondsToTicks(1.5);

    // Each N ticks, blood particles are created
    private static final int DEFAULT_TICKS_TILL_BLOOD_PARTICLES = Timings.secondsToTicks(0.25);

    // Progress to reach so survivor can regain a health state
    public static final float MAX_HEALING_PROGRESS = 16.0f;

    // Current hook stage
    private int hookStage = 0;

    // Time survivor has before hook stage progression (in ticks)
    private int sacrificeTime = STAGE_PROGRESSION_TIME;

    // Hook survivor is hooked on
    private Hook hookedOn = null;

    // Shows if survivor is being unhooked by someone
    private boolean beingUnhooked = false;

    // Time survivor has before dying from bleeding out
    private int bleedoutTime = STARTING_BLEEDOUT_TIME;

    // How survivor is doing
    private HealthState healthState = HealthState.HEALTHY;

    // Current healers
    private final ArrayList<Survivor> healers = new ArrayList<>();

    // Current healing/recovery progress
    private float healingProgress = 0.0f;

    private int ticksTillBloodParticles = DEFAULT_TICKS_TILL_BLOOD_PARTICLES;

    public Survivor(final Player player) {
        super(player, DEFAULT_SPEED);
    }

    public HealthState getHealthState() {
        return healthState;
    }

    public void setHealthState(final HealthState healthState) {
        this.healthState = healthState;

        // Notifying the game
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game != null)
            game.survivorsHealthChanged(this);
    }

    public void getHit() {
        // Interrupting any actions after receiving a hit
        interruptAction();
        healingProgress = 0.0f;

        // Creating hit effect
        player.playHurtAnimation(0);
        SoundManager.playForAll(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
        SoundManager.playForAll(player.getEyeLocation(), Sound.ENTITY_GHAST_HURT, 1, 1);

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
            setHealthState(HealthState.INJURED);
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
        makeRedScreen();
        setHealthState(HealthState.DYING);
        CrawlingPlugin.getPlugin().getCrawlingManager().startCrawling(player);
        player.sendMessage(ChatColor.RED + "You are now in dying state.");

        // Showing aura to everyone
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game != null) {
            final ArrayList<Survivor> survivors = game.getSurvivors();
            for (final Survivor survivor : survivors)
                survivor.addAura(this);
        }
    }

    // This function makes screen red
    public void makeRedScreen() {
        final WorldBorder worldBorder = player.getWorldBorder();
        if (worldBorder != null)
            worldBorder.setWarningDistance(Integer.MAX_VALUE);
    }

    // This function clears red screen
    public void clearRedScreen() {
        final WorldBorder worldBorder = player.getWorldBorder();
        if (worldBorder != null)
            worldBorder.setWarningDistance(5);
    }

    public void playTerrorRadius(final Location killerLocation) {
        final double distanceSquared = killerLocation.distanceSquared(getLocation());

        // Too far to hear terror radius
        if (distanceSquared > TERROR_FAR_SQUARED)
            return;

        // Progressing beat
        ++nextBeat;
        if (nextBeat < currentTerrorBeatDelay)
            return;

        // Playing beat
        SoundManager.playForOne(player, getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 0.5f);
        nextBeat = 0;

        // Calculating next beat
        final int terrorDelay;
        if (distanceSquared < TERROR_CLOSE_SQUARED)
            terrorDelay = TERROR_CLOSE_HEARTBEAT_DELAY;
        else if (distanceSquared < TERROR_CLOSER_SQUARED)
            terrorDelay = TERROR_CLOSER_HEARTBEAT_DELAY;
        else
            terrorDelay = TERROR_FAR_HEARTBEAT_DELAY;

        currentTerrorBeatDelay = secondBeat ? terrorDelay : TERROR_SECOND_BEAT_DELAY;
        secondBeat = !secondBeat;
    }

    // Returns current healing progress in range from 0.0 to 1.0
    public float getHealingProgressPercents() {
        return healingProgress / MAX_HEALING_PROGRESS;
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
                setHealthState(HealthState.HEALTHY);
                player.sendMessage(ChatColor.GREEN + "You are no longer injured.");
            }
            case DEEP_WOUND -> {
                setHealthState(HealthState.INJURED);
                player.sendMessage(ChatColor.GREEN + "You are no longer in deep wound.");
            }
            case DYING -> {
                setHealthState(HealthState.INJURED);
                CrawlingPlugin.getPlugin().getCrawlingManager().stopCrawling(player);
                player.sendMessage(ChatColor.GREEN + "You are no longer in dying state.");
                clearRedScreen();

                // Hiding dying aura
                final Game game = DeadByMinecraft.getPlugin().getGame();
                if (game != null) {
                    final ArrayList<Survivor> survivors = game.getSurvivors();
                    for (final Survivor survivor : survivors)
                        survivor.removeAura(this);
                }

                // Hiding bleed-out timer
                player.setLevel(0);
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

    // If survivor is in dying state, he can bleed-out after some time.
    // Must be called every tick
    public void processBleedOut() {
        if (healthState != HealthState.DYING)
            return;

        if (!isBeingHealed() && bleedoutTime > 0) {
            --bleedoutTime;

            if (bleedoutTime == 0) {
                die();
                return;
            }
        }

        // Showing bleed-out timer
        player.setLevel((int) Timings.ticksToSeconds(bleedoutTime));
    }

    // This function is called when survivor dies
    public void die() {
        CrawlingPlugin.getPlugin().getCrawlingManager().stopCrawling(player);
        clearRedScreen();

        setHealthState(HealthState.DEAD);
        player.setGameMode(GameMode.SPECTATOR);

        Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " has died!");
        player.sendTitle(ChatColor.RED + "DEAD", ChatColor.RED + "You bled out", 10, 70, 20);
    }

    public void trySelfUnhook() {
        final double randomResult = Math.random();
        if (randomResult < SELF_UNHOOK_CHANCE) {
            player.sendMessage(ChatColor.GREEN + "You have unhooked yourself!");
            getUnhooked(hookedOn);
            return;
        }

        player.sendMessage(ChatColor.RED + "Self-unhook attempt failed!");
        sacrificeTime -= FAILED_UNHOOK_TIME_PENALTY;
    }

    // Returns true if survivor can try to self-unhook
    public boolean canSelfUnhook() {
        return hookStage < 2 && sacrificeTime > 0;
    }

    // If survivor is hooked, he can be sacrificed after some time.
    // Must be called every tick
    public void processSacrifice() {
        if (healthState != HealthState.HOOKED)
            return;

        // Processing timer
        if (!beingUnhooked && sacrificeTime > 0)
            --sacrificeTime;

        // Progressing to the next stage
        if (sacrificeTime <= 0) {
            ++hookStage;
            sacrificeTime += STAGE_PROGRESSION_TIME;
            player.sendMessage(ChatColor.RED + "You have hit stage " + hookStage + "!");

            final Game game = DeadByMinecraft.getPlugin().getGame();
            if (game != null)
                game.checkIfDoomed();
        }

        // PLACE FOR CHECK IF PERSON HIT THE 3rd stage
        // RETURN AFTER THAT
        if (hookStage >= MAX_HOOK_STAGE) {
            getSacrificed();
            return;
        }

        // Showing sacrifice time
        final int timeLeftTicks = sacrificeTime + STAGE_PROGRESSION_TIME * (MAX_HOOK_STAGE - hookStage - 1);
        final int timeLeft = (int) Timings.ticksToSeconds(timeLeftTicks);
        player.setLevel(timeLeft);
    }

    // This function is called, when survivor is sacrificed
    public void getSacrificed() {
        if (!isAlive())
            return;

        if (hookedOn != null) {
            hookedOn.becomeBroken();
            hookedOn = null;
        }

        DeadByMinecraft.getPlugin().freezeManager.unFreeze(player.getUniqueId());
        setHealthState(HealthState.SACRIFICED);
        player.setGameMode(GameMode.SPECTATOR);

        Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " was sacrificed!");
        player.sendTitle(ChatColor.RED + "SACRIFICED", ChatColor.RED + "You were sacrificed", 10, 70, 20);
    }

    public void escape() {
        if (healthState == HealthState.DYING) {
            CrawlingPlugin.getPlugin().getCrawlingManager().stopCrawling(player);
            clearRedScreen();
        }

        setHealthState(HealthState.ESCAPED);
        player.setGameMode(GameMode.SPECTATOR);

        Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " has escaped!");
        player.sendTitle(ChatColor.GREEN + "ESCAPED",
                ChatColor.GREEN + "You have escaped", 10, 70, 20);
    }

    public boolean isBeingUnhooked() {
        if (action != null && action instanceof SelfUnhookAction)
            return true;
        return beingUnhooked;
    }

    public void setBeingUnhooked(final boolean beingUnhooked) {
        this.beingUnhooked = beingUnhooked;
    }

    public void enterLocker(final Locker locker) {
        // Starting locker enter action
        final int lockerEnterTime = player.isSneaking() ? SLOW_LOCKER_TIME : RUSHED_LOCKER_TIME;
        action = new LockerEnterAction(this, locker, lockerEnterTime, !player.isSneaking());
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    // This function handles locker entrance
    public void teleportToLocker(final Locker locker) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        // Hiding survivor from every player
        plugin.vanishManager.hide(player);

        // Teleporting survivor
        final Location lockerTop = locker.getLocation().clone().
                add(DeadByMinecraft.CENTERING, 2, DeadByMinecraft.CENTERING);

        // Making teleport smooth for camera
        final Location playerPitchAndYaw = player.getLocation();
        lockerTop.setPitch(playerPitchAndYaw.getPitch());
        lockerTop.setYaw(playerPitchAndYaw.getYaw());
        player.teleport(lockerTop);

        // Survivor is now in the locker
        locker.setHidingSurvivor(this);
        setMovementState(MovementState.IN_LOCKER);
        hideAllAuras();
    }

    public void leaveLocker(final Locker locker) {
        final int lockerLeaveTime = player.isSneaking() ? SLOW_LOCKER_TIME : RUSHED_LOCKER_TIME;
        action = new LockerLeaveAction(this, locker, lockerLeaveTime, !player.isSneaking());
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    // This function handles locker leaving
    public void teleportFromLocker(final Locker locker) {
        // Showing player
        DeadByMinecraft.getPlugin().vanishManager.show(player);
        locker.setHidingSurvivor(null);
        setMovementState(Character.MovementState.IDLE);

        // Teleporting to the door
        final Location exitLocation = locker.getBottomDoorBlock().getLocation().clone();

        // Making teleport smooth for camera
        final Location playerPitchAndYaw = player.getLocation();
        exitLocation.setPitch(playerPitchAndYaw.getPitch());
        exitLocation.setYaw(playerPitchAndYaw.getYaw());
        player.teleport(exitLocation.add(DeadByMinecraft.CENTERING,
                0, DeadByMinecraft.CENTERING));
        showAllAuras();
    }

    public void getHooked(final Hook hook) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        hook.hook(this);
        plugin.freezeManager.freeze(player.getUniqueId(), false);

        final Location teleportLocation = hook.getHook().getRelative(0, -2, 0).getLocation().clone();
        player.teleport(teleportLocation.add(DeadByMinecraft.CENTERING, 0, DeadByMinecraft.CENTERING));
        setHealthState(HealthState.HOOKED);
        hookedOn = hook;
        sacrificeTime = STAGE_PROGRESSION_TIME;
        ++hookStage;

        // Showing auras to everyone
        final Game game = plugin.getGame();
        if (game != null) {
            final ArrayList<Survivor> survivors = game.getSurvivors();
            for (final Survivor survivor : survivors)
                survivor.addAura(this);
            game.getKiller().addAura(this);
        }

        if (canSelfUnhook())
            player.sendMessage(ChatColor.YELLOW + "You can try to self-unhook!");

        // Hooking sounds
        SoundManager.playForAll(player.getEyeLocation(), Sound.ENTITY_GHAST_HURT, 100f, 1f);
    }

    public void getUnhooked(final Hook hook) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        if (hookedOn != null) {
            hook.unHook();
            hookedOn = null;
        }

        plugin.freezeManager.unFreeze(player.getUniqueId());
        setHealthState(HealthState.INJURED);

        // Hiding sacrifice time
        player.setLevel(0);

        // Hiding aura from everyone
        final Game game = plugin.getGame();
        if (game != null) {
            final ArrayList<Survivor> survivors = game.getSurvivors();
            for (final Survivor survivor : survivors)
                survivor.removeAura(this);
            game.getKiller().removeAura(this);
        }
    }

    // Returns hook on which survivor is hooked on
    public Hook getHook() {
        return hookedOn;
    }

    public void startRepairing(final Generator generator, final Generator.GeneratorSide side) {
        side.setPlayer(this);
        action = new RepairAction(this, generator, side);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    public void startHealing(final Survivor healingTarget) {
        action = new HealAction(this, healingTarget);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    public void startRecovering() {
        action = new RecoverAction(this);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    public boolean isBeingHealed() {
        return !healers.isEmpty();
    }

    public void addToHealersList(final Survivor survivor) {
        healers.add(survivor);
    }

    public void removeFromHealersList(final Survivor survivor) {
        healers.removeIf(n -> (n.player.getUniqueId().equals(survivor.getPlayer().getUniqueId())));
    }

    public void startUnhooking(final Survivor survivor) {
        action = new SurvivorUnhookAction(this, survivor);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
        player.sendMessage(ChatColor.GREEN + "You have started unhooking. Don't move!");
    }

    @Override
    public int getVaultTimeTicks(final boolean isRushed) {
        return isRushed ? FAST_VAULT_TIME : SLOW_VAULT_TIME;
    }

    @Override
    public void startOpening(final ExitGate exitGate) {
        action = new SurvivorOpenExitAction(this, exitGate);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
        player.sendMessage(ChatColor.GREEN + "You have started opening the exit gates. Don't move!");
    }

    // Escaping through the hatch
    @Override
    public void useHatch(final Hatch hatch) {
        escape();
    }

    // Returns true if survivor is hittable
    // It doesn't show if he is damageable
    public boolean isHittable() {
        return (healthState != HealthState.DYING && healthState != HealthState.BEING_CARRIED &&
                movementState != MovementState.IN_LOCKER);
    }

    // Returns true if survivor can be grabbed
    // He must be doing specific actions
    public boolean isGrabbable() {
        if (action == null)
            return false;

        if (action instanceof LockerAction)
            return true;

        if (action instanceof RepairAction)
            return true;

        return action instanceof SurvivorOpenExitAction;
    }

    // Returns true if survivor is neither healthy, injured, nor deep wounded
    public boolean isIncapacitated() {
        return (healthState != HealthState.HEALTHY && healthState != HealthState.INJURED &&
                healthState != HealthState.DEEP_WOUND);
    }

    // Returns true, if survivor is considered alive (and he didn't escape yet)
    public boolean isAlive() {
        return healthState != HealthState.DEAD && healthState != HealthState.DISCONNECTED &&
                healthState != HealthState.SACRIFICED && healthState != HealthState.ESCAPED;
    }

    // Returns true, if survivor is considered downed (for the endgame collapse)
    public boolean isDowned() {
        return healthState == HealthState.DYING || healthState == HealthState.BEING_CARRIED ||
                healthState == HealthState.HOOKED;
    }

    // Returns true, if survivor can be healed
    // It works if survivor is injured and isn't interacting with anything
    public boolean isHealable() {
        if (healthState != HealthState.INJURED && healthState != HealthState.DEEP_WOUND &&
                healthState != HealthState.DYING)
            return false;

        if (movementState != MovementState.IDLE)
            return false;

        return (action == null || action instanceof RecoverAction);
    }

    @Override
    public boolean canInteract() {
        if (isIncapacitated())
            return false;

        if (isBeingHealed()) {
            player.sendMessage(ChatColor.YELLOW + "You can't interact with anything while being healed. " +
                    "Sneak (press Shift) or start moving to force others to stop healing you");
            return false;
        }

        if (movementState != MovementState.IDLE)
            return false;

        return action == null;
    }

    @Override
    public boolean canInteractWithLocker() {
        if (isIncapacitated())
            return false;

        if (isBeingHealed()) {
            player.sendMessage(ChatColor.YELLOW + "You can't interact with anything while being healed. " +
                    "Sneak (press Shift) or start moving to force others to stop healing you");
            return false;
        }

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
    public boolean canInteractWithHatch() {
        if (movementState != MovementState.IDLE)
            return false;

        // Player can escape, even while in the dying state
        if ((healthState != HealthState.HEALTHY && healthState != HealthState.INJURED &&
                healthState != HealthState.DEEP_WOUND && healthState != HealthState.DYING))
            return false;

        return action == null;
    }

    @Override
    public boolean canInteractWithSurvivor() {
        return canInteract();
    }
}

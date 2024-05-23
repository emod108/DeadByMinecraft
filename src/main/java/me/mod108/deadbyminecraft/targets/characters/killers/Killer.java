package me.mod108.deadbyminecraft.targets.characters.killers;

import me.mod108.crawlingplugin.CrawlingPlugin;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.actions.PalletBreakAction;
import me.mod108.deadbyminecraft.actions.WiggleAction;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import me.mod108.deadbyminecraft.targets.props.Hook;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public abstract class Killer extends Character {
    // For how much killer moves forward after attacking
    // Doesn't affect range attack
    public static final double LUNGE_STRENGTH = 0.5;

    // How fast killers move by default
    public static final float DEFAULT_SPEED = 1.15f;

    // Speed, at which killer moves while carrying someone
    public static final float CARRYING_SPEED = 0.92f;

    // How far can killers attack
    public static final double ATTACK_DISTANCE = 1.6;

    // For how long killers vault a window in ticks
    private static final int DEFAULT_VAULT_TIME = Timings.secondsToTicks(1.5);

    // Default attack cooldown after successful hit
    private static final int HIT_ATTACK_COOLDOWN = Timings.secondsToTicks(2.7);

    // Moving speed modifier after successful attack
    private static final float HIT_ATTACK_SPEED = 0.125f;

    // Default attack cooldown after missed hit
    private static final int MISS_ATTACK_COOLDOWN = Timings.secondsToTicks(1.5);

    // Moving speed modifier after missed attack
    private static final float MISS_ATTACK_SPEED = 0.25f;

    // Default pallet stun time
    public static final int PALLET_STUN_TIME = Timings.secondsToTicks(2.0);

    // Default stun time after survivor wiggles free
    public static final int WIGGLE_STUN_TIME = Timings.secondsToTicks(3.0);

    // All killers available
    public static final String[] KILLER_NAMES = {
            "Trapper"
    };

    // Survivor, which is being carried by the killer
    private Survivor carriedSurvivor = null;

    // Cool-down on the killer's attacks (in ticks)
    private int attackCooldownTime = 0;

    // Last attack cooldown time, needed for the progress bar
    private int lastAttackCooldownTime = HIT_ATTACK_COOLDOWN;

    // Last stun time, needed for the progress bar
    private int lastStunTime = 0;

    // Time for which the killer gets stunned (ticks)
    private int stunTime = 0;

    public Killer(final Player player, final float baseSpeed) {
        super(player, baseSpeed, DEFAULT_VAULT_TIME);
    }

    @Override
    public float getBaseSpeed() {
        return carriedSurvivor == null ? super.getBaseSpeed() : CARRYING_SPEED;
    }

    @Override
    public float getCurrentSpeed() {
        float currentSpeed = super.getCurrentSpeed();

        // If killer is on attack cool-down, then we decrease the speed
        if (isOnAttackCooldown()) // If hit was successful, speed penalty is harsher
            currentSpeed *= lastAttackCooldownTime == HIT_ATTACK_COOLDOWN ? HIT_ATTACK_SPEED : MISS_ATTACK_SPEED;

        return currentSpeed;
    }

    // Helper function to create killer's items faster
    protected static ItemStack createKillerItem(final Material material, final String name) {
        final ItemStack itemStack = new ItemStack(material);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    public abstract void applyKillerKit();

    public abstract String getKillerName();

    public abstract Sound getStunSounds();

    public void hit(final Survivor survivor) {
        // Launching player in the direction he looks
        final Vector lungeVector = player.getLocation().getDirection().clone();
        lungeVector.multiply(LUNGE_STRENGTH); // Lunge should be a bit weaker
        lungeVector.setY(player.getVelocity().getY()); // Making is so upwards velocity isn't influenced
        player.setVelocity(lungeVector);

        // Attack sound
        SoundManager.playForAll(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1, 1);

        // Missed attack
        if (survivor == null) {
            setAttackCooldown(MISS_ATTACK_COOLDOWN);
            player.sendMessage(ChatColor.RED + "You missed!");
            return;
        }

        // Successful attack
        survivor.getHit();
        setAttackCooldown(HIT_ATTACK_COOLDOWN);
        player.sendMessage(ChatColor.GREEN + "You hit " + survivor.getPlayer().getDisplayName() + "!");
    }

    public int getAttackCooldownTime() {
        return attackCooldownTime;
    }

    public void decrementAttackCooldownTime() {
        if (attackCooldownTime > 0)
            --attackCooldownTime;
    }

    public boolean isOnAttackCooldown() {
        return attackCooldownTime > 0;
    }

    public void setAttackCooldown(final int attackCooldownTime) {
        lastAttackCooldownTime = attackCooldownTime;
        this.attackCooldownTime = attackCooldownTime;
    }

    // Returns killer's stun recover progress from 0.0 to 1.0
    public float getAttackRecoverProgress() {
        // Avoiding division by 0
        if (lastAttackCooldownTime == 0)
            return 1.0f;

        return (float) (1.0 - Timings.ticksToSeconds(attackCooldownTime) /
                Timings.ticksToSeconds(lastAttackCooldownTime));
    }

    public int getStunTime() {
        return stunTime;
    }

    public boolean isStunned() {
        return stunTime > 0;
    }

    // Returns killer's stun recover progress from 0.0 to 1.0
    public float getStunRecoverProgress() {
        // Avoiding division by 0
        if (lastStunTime == 0)
            return 1.0f;

        return (float) (1.0 - Timings.ticksToSeconds(stunTime) / Timings.ticksToSeconds(lastStunTime));
    }

    // Checks if the killer can be stunned
    public boolean canBeStunned() {
        // Killer can't be stunned while doing actions
        if (action != null)
            return false;

        // Killer can't be stunned if he is already
        return !isStunned();
    }

    public void decrementStunTime() {
        if (stunTime > 0) {
            --stunTime;

            // Allowing to move if the killer is no longer stunned
            if (stunTime == 0)
                DeadByMinecraft.getPlugin().freezeManager.unFreeze(player);
        }
    }

    public void getStunned() {
        int stunTime;

        // If the killer was carrying someone, that survivor escapes
        if (carriedSurvivor != null) {
            getWiggledFrom();
            stunTime = WIGGLE_STUN_TIME;
        } else {
            stunTime = PALLET_STUN_TIME;
        }

        // Setting stun timer
        lastStunTime = stunTime;
        this.stunTime = stunTime;
        attackCooldownTime = 0;

        // Freezing killer and playing stun sound
        DeadByMinecraft.getPlugin().freezeManager.freeze(player);
        SoundManager.playForAll(player.getLocation(), getStunSounds(), 1, 1);
        player.sendMessage(ChatColor.RED + "You were stunned!");
    }

    // Returns carrying survivor or null if no survivor carried
    public Survivor getCarriedSurvivor() {
        return carriedSurvivor;
    }

    public void pickUp(final Survivor survivor) {
        carriedSurvivor = survivor;
        survivor.setHealthState(Survivor.HealthState.BEING_CARRIED);

        // Making so player stops crawling
        final Player survivorPlayer = survivor.getPlayer();
        CrawlingPlugin.getPlugin().getCrawlingManager().stopCrawling(survivorPlayer);

        // Spawning a slime to ride
        final World world = Bukkit.getWorld("world");
        if (world == null) {
            System.err.println("While picking up survivor couldn't get the world to spawn a slime!");
            return;
        }
        final Slime slime = (Slime) world.spawnEntity(player.getLocation(), EntityType.SLIME);
        slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                PotionEffect.INFINITE_DURATION, 1, false, false));
        slime.setSize(1);
        slime.setAI(false);
        slime.setInvulnerable(true);

        // Slime rides killer, survivor rides slime
        player.addPassenger(slime);
        slime.addPassenger(survivorPlayer);
        player.sendMessage(ChatColor.GREEN + "Picked up " + survivor.getPlayer().getDisplayName());

        // Creating wiggle action
        survivorPlayer.sendMessage(ChatColor.YELLOW + "You have been picked up by the killer. " +
                "Press SHIFT (dismount button) to start trying to escape him.");
        final WiggleAction wiggleAction = new WiggleAction(survivor, this);
        survivor.setAction(wiggleAction);
        wiggleAction.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    public void stopCarrying() {
        if (carriedSurvivor == null)
            return;

        // Making it so survivor is no longer carried
        final Survivor carried = carriedSurvivor;
        carriedSurvivor = null;

        // Getting slime upon which carried player sits
        final Entity slimeEntity = carried.getPlayer().getVehicle();
        if (slimeEntity == null)
            return;
        slimeEntity.eject();
        slimeEntity.remove();
    }

    private void getWiggledFrom() {
        final Survivor survivor = carriedSurvivor;
        if (carriedSurvivor == null)
            return;

        stopCarrying();
        survivor.setHealthState(Survivor.HealthState.INJURED);
    }

    // Hook currently carrying survivor
    public void hookSurvivor(final Hook hook) {
        // No survivors carried
        if (carriedSurvivor == null)
            return;

        // Getting carried survivor
        final Survivor survivor = carriedSurvivor;
        stopCarrying();

        // Hooking survivor
        survivor.getHooked(hook);
        player.sendMessage(ChatColor.GREEN + "Survivor hooked!");
    }

    public void startBreakingPallet(final Pallet target) {
        action = new PalletBreakAction(this, target);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
        player.sendMessage(ChatColor.YELLOW + "Breaking a pallet");
    }

    @Override
    public void startOpening(final ExitGate exitGate) {}

    @Override
    public boolean canInteract() {
        if (carriedSurvivor != null)
            return false;

        if (isOnAttackCooldown())
            return false;

        if (isStunned())
            return false;

        return action == null;
    }

    @Override
    public boolean canInteractWithLocker() {
        return false;
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
        return false;
    }

    @Override
    public boolean canInteractWithExitGate() {
        return false;
    }

    @Override
    public boolean canInteractWithSurvivor() {
        return canInteract();
    }

    // Checks if killer can hit survivors
    public boolean canHitSurvivors() {
        if (isOnAttackCooldown())
            return false;

        if (isStunned())
            return false;

        return action == null;
    }
}

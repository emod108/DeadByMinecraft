package me.mod108.deadbyminecraft.targets.characters;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.actions.Action;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.Target;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import me.mod108.deadbyminecraft.targets.props.Hatch;
import me.mod108.deadbyminecraft.targets.props.vaultable.Vaultable;
import me.mod108.deadbyminecraft.actions.VaultAction;
import me.mod108.deadbyminecraft.utility.Game;
import me.mod108.deadbyminecraft.utility.MovementSpeed;
import me.mod108.deadbyminecraft.utility.SpeedModifier;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class Character implements Target {
    // All possible movement states
    public enum MovementState { IDLE, IN_LOCKER }

    // Distance, from which an action can be done
    private static final double ACTION_DISTANCE = 1.8;
    public static final double ACTION_DISTANCE_SQUARED = ACTION_DISTANCE * ACTION_DISTANCE;

    // Player who plays as this character
    protected Player player;

    // Base speed at which this character moves
    private final float baseSpeed;

    // Speed modifiers
    private final ArrayList<SpeedModifier> speedModifiers = new ArrayList<>();

    // Shows if speed effects are active on this player
    private boolean isSpeedActive = false;

    // Is he moving or is even he able to
    protected MovementState movementState = MovementState.IDLE;

    // Current character's action
    protected Action action = null;

    // This is a list of players, auras of which are visible to this player
    final ArrayList<Character> auras = new ArrayList<>();
    boolean aurasVisible = true;

    public Character(final Player player, final float baseSpeed) {
        this.player = player;
        this.baseSpeed = baseSpeed;
    }

    // Checks if it's the same player for both characters
    public boolean samePlayer(final Character otherPlayer) {
        if (otherPlayer == null)
            return false;
        return (player.getUniqueId().equals(otherPlayer.getPlayer().getUniqueId()));
    }

    // Adds an aura
    public void addAura(final Character aura) {
        // Player can't see his own aura
        if (samePlayer(aura))
            return;

        // Checking if aura is already visible
        for (final Character visibleAura : auras) {
            if (visibleAura.samePlayer(aura))
                return;
        }
        auras.add(aura);
        try {
            if (aurasVisible)
                DeadByMinecraft.getPlugin().glowingEntities.setGlowing(aura.getPlayer(), player);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // Removes an aura
    public void removeAura(final Character aura) {
        for (int i = 0; i < auras.size(); ++i) {
            if (auras.get(i).samePlayer(aura)) {
                auras.remove(i);
                try {
                    if (aurasVisible)
                        DeadByMinecraft.getPlugin().glowingEntities.unsetGlowing(aura.getPlayer(), player);
                } catch (final ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
    }

    // Removes all visible auras
    public void removeAllAuras() {
        if (aurasVisible) {
            for (final Character visibleAura : auras) {
                try {
                    DeadByMinecraft.getPlugin().glowingEntities.unsetGlowing(visibleAura.getPlayer(), player);
                } catch (final ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        auras.clear();
    }

    // Hides all visible auras
    public void hideAllAuras() {
        if (!aurasVisible)
            return;

        for (final Character visibleAura : auras) {
            try {
                DeadByMinecraft.getPlugin().glowingEntities.unsetGlowing(visibleAura.getPlayer(), player);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        aurasVisible = false;
    }

    // Shows all visible auras
    public void showAllAuras() {
        if (aurasVisible)
            return;
        for (final Character visibleAura : auras) {
            try {
                DeadByMinecraft.getPlugin().glowingEntities.setGlowing(visibleAura.getPlayer(), player);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        aurasVisible = true;
    }

    public boolean isInArea(final double x1, final double x2,
                            final double y1, final double y2,
                            final double z1, final double z2) {
        final Location pLoc = player.getLocation();
        return pLoc.getX() > x1 && pLoc.getX() < x2 &&
                pLoc.getY() > y1 && pLoc.getY() < y2 &&
                pLoc.getZ() > z1 && pLoc.getZ() < z2;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public float getCurrentSpeed() {
        float currentSpeed = getBaseSpeed();

        // Adding speed modifiers
        for (final SpeedModifier modifier : speedModifiers)
            currentSpeed += modifier.getValue();

        return currentSpeed;
    }

    // Sets if speed effect are active on this player
    // On true base speed and speed modifiers are working
    // On false only default minecraft speed is being left
    public void setIsSpeedActive(final boolean isActive) {
        isSpeedActive = isActive;
        if (isSpeedActive)
            applySpeed();
        else
            clearSpeed();
    }

    public void applySpeed() {
        final MovementSpeed speed = new MovementSpeed(getCurrentSpeed());
        speed.applyToPlayer(player);
    }

    public void clearSpeed() {
        MovementSpeed.clearPlayerSpeed(player);
    }

    // Updates all current speed modifiers and removes those which duration came to end
    public void updateSpeed() {
        // Updating time on each modifier
        for (final SpeedModifier modifier : speedModifiers)
            modifier.decrementTime();

        // Removing modifiers which duration came to end
        speedModifiers.removeIf(modifier -> modifier.getTime() == SpeedModifier.DURATION_END);
        applySpeed();
    }

    // Adds a new speed modifier
    public void addSpeedModifier(final SpeedModifier modifier) {
        speedModifiers.add(modifier);
    }

    // Removes a speed modifier
    public void removeSpeedModifier(final String name) {
        speedModifiers.removeIf(modifier -> modifier.getName().equals(name));
    }

    // Returns vault time in ticks. You can check if vault is rushed
    public abstract int getVaultTimeTicks(final boolean isRushed);

    public MovementState getMovementState() {
        return movementState;
    }

    public void setMovementState(final MovementState movementState) {
        this.movementState = movementState;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(final Action action) {
        this.action = action;
    }

    public void interruptAction() {
        if (action != null)
            action.interrupt();
    }

    public void cancelAction() {
        if (action != null)
            action.end();
    }

    public void vault(final Vaultable vaultable) {
        // Checking if vaulting is rushed. Does nothing if it's the killer
        final boolean isRushed = !player.isSneaking() || this instanceof Killer;

        // Creating vaulting action
        action = new VaultAction(this, vaultable, getVaultTimeTicks(isRushed));
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);

        // Playing vaulting sound if it's a rushed action
        if (isRushed)
            SoundManager.playForAll(vaultable.getLocation(), vaultable.getVaultingSound(), 1f, 1f);
    }

    // Method of interaction with exit gates
    public abstract void startOpening(final ExitGate exitGate);

    // Method of interaction with hatch
    public abstract void useHatch(final Hatch hatch);

    // General method so code won't be repeated
    public abstract boolean canInteract();

    // Returns true if player can interact with lockers
    public abstract boolean canInteractWithLocker();

    // Returns true if player can interact with windows
    public abstract boolean canInteractWithWindow();

    // Returns true if player can interact with pallets
    public abstract boolean canInteractWithPallet();

    // Returns true if player can interact with generator
    public abstract boolean canInteractWithGenerator();

    // Returns true if player can interact with exit gates
    public abstract boolean canInteractWithExitGate();

    // Returns true, if player can interact with the hatch
    public abstract boolean canInteractWithHatch();

    // Returns true if player can interact with survivor (healing, unhooking, picking up, etc.)
    public abstract boolean canInteractWithSurvivor();
}
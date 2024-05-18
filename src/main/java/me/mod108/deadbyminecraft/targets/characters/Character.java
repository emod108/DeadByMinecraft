package me.mod108.deadbyminecraft.targets.characters;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.actions.Action;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.Target;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.targets.props.vaultable.Vaultable;
import me.mod108.deadbyminecraft.targets.props.vaultable.Window;
import me.mod108.deadbyminecraft.actions.VaultAction;
import me.mod108.deadbyminecraft.utility.MovementSpeed;
import me.mod108.deadbyminecraft.utility.SpeedModifier;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class Character implements Target {
    // All possible movement states
    public enum MovementState { IDLE, IN_LOCKER }

    // Distance, from which an action can be done
    public static final double ACTION_MAX_DISTANCE = 1.8;

    // Player who plays as this character
    protected Player player;

    // Base speed at which this character moves
    private final float baseSpeed;

    // Speed modifiers
    private final ArrayList<SpeedModifier> speedModifiers = new ArrayList<>();

    // Shows if speed effects are active on this player
    private boolean isSpeedActive = false;

    // How many ticks needed to vault a vaultable prop
    private final int vaultTimeTicks;

    // Is he moving or is even he able to
    protected MovementState movementState = MovementState.IDLE;

    // Current character's action
    protected Action action = null;

    public Character(final Player player, final float baseSpeed, final int vaultTimeTicks) {
        this.player = player;

        this.baseSpeed = baseSpeed;
        this.vaultTimeTicks = vaultTimeTicks;
    }

    // Checks if it's the same player for both characters
    public boolean samePlayer(final Character otherPlayer) {
        if (otherPlayer == null)
            return false;
        return (player.getUniqueId().equals(otherPlayer.getPlayer().getUniqueId()));
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

    // Shows if speed effects are active on this player
    public boolean getIsSpeedActive() {
        return isSpeedActive;
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

    public int getVaultTimeTicks() {
        return vaultTimeTicks;
    }

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
        vaultable.setVaultingPlayer(this);
        action = new VaultAction(this, vaultable);
        action.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);

        if (vaultable instanceof Pallet)
            SoundManager.playForAll(vaultable.getLocation(), Pallet.VAULT_SOUND, 1.0f, 1.0f);
        else
            SoundManager.playForAll(vaultable.getLocation(), Window.VAULT_SOUND, 1.0f, 1.0f);
    }

    public abstract void startOpening(final ExitGate exitGate);

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

    // Returns true if player can interact with survivor (healing, unhooking, picking up, etc.)
    public abstract boolean canInteractWithSurvivor();
}
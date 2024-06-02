package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.targets.props.vaultable.Vaultable;
import me.mod108.deadbyminecraft.utility.ProgressBar;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VaultAction extends Action {
    // How far vaulting should teleport the player
    private static final double VAULT_DISTANCE = 2.0;

    // How high the player spawns to vault
    private static final double VAULT_MAX_HEIGHT = 2.0;

    // How many ticks already passed
    private int counter = 0;

    // For how long vaulting must go
    private final int vaultTimeTicks;

    // Location teleport to on next tick
    private final Location teleportLocation;

    // Making it so player vaults in a curve instead of in a straight line
    private final double slope;

    // Distance, player should be teleported for
    private final Vector teleportVector = new Vector(0, 0, 0);

    public VaultAction(final Character performer, final Vaultable vaultable, final int vaultTimeTicks) {
        super(performer, vaultable);

        // Calculating vaulting time
        this.vaultTimeTicks = vaultTimeTicks;
        final double distancePerTick = VAULT_DISTANCE / vaultTimeTicks;

        // Calculating how player should be teleported
        // In case if it's a pallet, its real location is different from prop's location
        teleportLocation = (vaultable instanceof final Pallet pallet) ? pallet.getPallet().getLocation().clone() :
                vaultable.getLocation().clone();

        // Getting locations
        final Location vaultableLocation = vaultable.getLocation();
        final Location performerLocation = performer.getLocation();
        if (vaultable.canVaultEastAndWest()) {
            // Vaulting EAST
            if (performerLocation.getX() - 0.1 < vaultableLocation.getX()) {
                teleportLocation.add(-DeadByMinecraft.CENTERING, 0, DeadByMinecraft.CENTERING);
                teleportVector.setX(distancePerTick);
            } else { // Vaulting WEST
                teleportLocation.add(DeadByMinecraft.CENTERING * 3, 0, DeadByMinecraft.CENTERING);
                teleportVector.setX(-distancePerTick);
            }
        } else {
            // Vaulting SOUTH
            if (performerLocation.getZ() - 0.1 < vaultableLocation.getZ()) {
                teleportLocation.add(DeadByMinecraft.CENTERING, 0, -DeadByMinecraft.CENTERING);
                teleportVector.setZ(distancePerTick);
            } else { // Vaulting NORTH
                teleportLocation.add(DeadByMinecraft.CENTERING, 0, DeadByMinecraft.CENTERING * 3);
                teleportVector.setZ(-distancePerTick);
            }
        }
        slope = VAULT_MAX_HEIGHT / vaultTimeTicks;
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Making camera movement more smooth by keeping camera pitch and yaw
        final Location performerLocation = performer.getLocation();
        teleportLocation.setPitch(performerLocation.getPitch());
        teleportLocation.setYaw(performerLocation.getYaw());

        // Height curve
        if (counter <= vaultTimeTicks / 2)
            teleportLocation.add(0, slope, 0);
        else
            teleportLocation.add(0, -slope, 0);

        // Moving player
        teleportLocation.add(teleportVector);
        performer.getPlayer().teleport(teleportLocation);

        // Cancel condition
        ++counter;
        ProgressBar.setProgress(performer.getPlayer(), getProgress());
        if (counter >= vaultTimeTicks)
            end();
    }

    @Override
    public void end() {
        super.end();
    }

    @Override
    public float getProgress() {
        return (float) counter / vaultTimeTicks;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public String getActionBar() {
        return "";
    }
}

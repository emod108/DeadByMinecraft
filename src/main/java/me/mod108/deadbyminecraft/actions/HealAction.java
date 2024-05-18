package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.utility.ProgressBar;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class HealAction extends Action {
    // Healing progress achieved per tick
    public static final float HEALING_SPEED = 1.0f / Timings.TICKS_PER_SECOND;

    // If healer or healing target moves, then action stops
    private Location healerLocation = null;
    private Location targetLocation = null;

    public HealAction(final Survivor healer, final Survivor target) {
        super(healer, target);
        target.addToHealersList(healer);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Showing healing progress to the one who is being healed
        final Survivor healedTarget = (Survivor) target;
        ProgressBar.setProgress(healedTarget.getPlayer(), getProgress());

        // If target can't be healed anymore, we stop
        if (!healedTarget.isHealable()) {
            end();
            return;
        }

        // Can't heal sneaking players
        if (healedTarget.getPlayer().isSneaking()) {
            end();
            performer.getPlayer().sendMessage(ChatColor.RED + "Target doesn't want to be healed.");
            return;
        }

        // Getting starting location
        if (healerLocation == null)
            healerLocation = performer.getLocation();
        if (targetLocation == null)
            targetLocation = healedTarget.getLocation();

        // Checking if survivor moved
        Location currentLocation = performer.getLocation();
        if (currentLocation.getX() != healerLocation.getX() || currentLocation.getZ() != healerLocation.getZ() ||
                currentLocation.getY() != healerLocation.getY()) {
            end();
            performer.getPlayer().sendMessage(ChatColor.RED + "You have moved! Healing was canceled.");
            healedTarget.getPlayer().sendMessage(ChatColor.YELLOW + performer.getPlayer().getDisplayName() +
                    ChatColor.RED + " has moved and is no longer healing you");
            return;
        }

        currentLocation = healedTarget.getLocation();
        if (currentLocation.getX() != targetLocation.getX() || currentLocation.getZ() != targetLocation.getZ() ||
                currentLocation.getY() != targetLocation.getY()) {
            end();
            performer.getPlayer().sendMessage(ChatColor.RED + "Healing target has moved! Healing was canceled.");
            healedTarget.getPlayer().sendMessage(ChatColor.RED + "You have moved so " + ChatColor.YELLOW +
                    performer.getPlayer().getDisplayName() + ChatColor.RED + " is no longer healing you");
            return;
        }

        healedTarget.addHealingProgress(HEALING_SPEED);
    }

    @Override
    public void end() {
        super.end();
        final Survivor healedSurvivor = (Survivor) target;

        if (healedSurvivor.getHealthState() != Survivor.HealthState.DYING)
            ProgressBar.resetProgress(healedSurvivor.getPlayer());
        healedSurvivor.removeFromHealersList((Survivor) performer);
    }

    @Override
    public float getProgress() {
        return ((Survivor) target).getHealingProgressPercents();
    }
}

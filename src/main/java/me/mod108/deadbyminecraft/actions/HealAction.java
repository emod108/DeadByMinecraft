package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.utility.ActionBar;
import me.mod108.deadbyminecraft.utility.ProgressBar;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class HealAction extends Action {
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
        final String actionBarStr = ChatColor.GREEN + "You are being healed by " + performer.getPlayer().getName();
        ActionBar.setActionBar(healedTarget.getPlayer(), actionBarStr);

        // If target can't be healed anymore, we stop
        if (!healedTarget.isHealable()) {
            end();
            return;
        }

        // If survivor can't perform healing anymore, we stop
        if (((Survivor) performer).isIncapacitated()) {
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
            healedTarget.getPlayer().sendMessage(ChatColor.RED + performer.getPlayer().getName() +
                    " has moved and is no longer healing you");
            return;
        }

        currentLocation = healedTarget.getLocation();
        if (currentLocation.getX() != targetLocation.getX() || currentLocation.getZ() != targetLocation.getZ() ||
                currentLocation.getY() != targetLocation.getY()) {
            end();
            performer.getPlayer().sendMessage(ChatColor.RED + healedTarget.getPlayer().getName() +
                    "has moved! Healing was canceled.");
            healedTarget.getPlayer().sendMessage(ChatColor.RED + "You have moved! " + performer.getPlayer().getName()
                    + " is no longer healing you");
            return;
        }

        healedTarget.addHealingProgress(ACTION_SPEED);
    }

    @Override
    public void end() {
        super.end();
        final Survivor healedSurvivor = (Survivor) target;

        if (healedSurvivor.getHealthState() != Survivor.HealthState.DYING)
            ProgressBar.resetProgress(healedSurvivor.getPlayer());
        ActionBar.resetActionBar(healedSurvivor.getPlayer());
        healedSurvivor.removeFromHealersList((Survivor) performer);
    }

    @Override
    public float getProgress() {
        return ((Survivor) target).getHealingProgressPercents();
    }

    @Override
    public String getActionBar() {
        return ChatColor.GREEN + "Healing " + ((Survivor) target).getPlayer().getName();
    }
}

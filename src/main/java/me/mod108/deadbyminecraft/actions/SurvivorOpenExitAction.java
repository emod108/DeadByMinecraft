package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class SurvivorOpenExitAction extends Action {
    // Repair speed achieved per tick
    private static final float OPEN_SPEED = 1.0f / Timings.TICKS_PER_SECOND;

    // If players moves, exit gates opening process stops
    private Location startLocation = null;

    public SurvivorOpenExitAction(final Survivor performer, final ExitGate exitGate) {
        super(performer, exitGate);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Getting starting location
        if (startLocation == null)
            startLocation = performer.getLocation();

        // Checking if survivor moved
        final Location location = performer.getLocation();
        if (location.getX() != startLocation.getX() || location.getZ() != startLocation.getZ() ||
                location.getY() != startLocation.getY()) {
            end();
            performer.getPlayer().sendMessage(ChatColor.RED + "You have moved! Opening was canceled.");
            return;
        }

        ((ExitGate) target).addOpenProgress(OPEN_SPEED);
    }

    @Override
    public void end() {
        super.end();
        ((ExitGate) target).setInteractingPlayer(null);
    }

    @Override
    public float getProgress() {
        return ((ExitGate) target).getProgressPercents();
    }
}

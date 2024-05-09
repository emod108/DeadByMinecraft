package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.props.Hook;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class SurvivorUnhookAction extends Action {
    // Unhook progress achieved per second
    private static final float UNHOOK_PROGRESS_PER_SECOND = 1.0f;

    // Repair speed achieved per tick
    private static final float UNHOOK_PROGRESS_PER_TICK = Timings.secondsToTicks(UNHOOK_PROGRESS_PER_SECOND);

    // Max unhook progress
    private static final float MAX_UNHOOK_PROGRESS = 1.0f;

    // current unhook progress
    private float unhookProgress = 0.0f;

    // If players moves, unhooking process stops
    private Location startLocation = null;

    public SurvivorUnhookAction(final Survivor performer, final Survivor unhookTarget) {
        super(performer, unhookTarget);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Getting starting location
        if (startLocation == null)
            startLocation = performer.getLocation();

        // Checking if performer moved
        final Location location = performer.getLocation();
        if (location.getX() != startLocation.getX() || location.getZ() != startLocation.getZ() ||
                location.getY() != startLocation.getY()) {
            end();
            performer.getPlayer().sendMessage(ChatColor.RED + "You have moved! Unhooking was canceled.");
            return;
        }

        final Survivor unhookTarget = (Survivor) target;
        final Hook hook = unhookTarget.getHook();

        // If the unhook target is not hooked on any hook, then stop the action
        if (hook == null) {
            end();
            return;
        }

        // Unhook when progress reaches maximum
        unhookProgress += UNHOOK_PROGRESS_PER_TICK;
        if (unhookProgress >= MAX_UNHOOK_PROGRESS) {
            unhookTarget.getUnhooked(hook);
            end();
        }
    }

    @Override
    public float getProgress() {
        return unhookProgress / MAX_UNHOOK_PROGRESS;
    }
}

package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.props.Hook;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class SurvivorUnhookAction extends Action {
    // Unhook progress achieved per tick
    private static final float UNHOOK_SPEED = 1.0f / Timings.TICKS_PER_SECOND;

    // Max unhook progress
    private static final float MAX_UNHOOK_PROGRESS = 1.0f;

    // current unhook progress
    private float unhookProgress = 0.0f;

    // If players moves, unhooking process stops
    private Location startLocation = null;

    public SurvivorUnhookAction(final Survivor performer, final Survivor unhookTarget) {
        super(performer, unhookTarget);
        unhookTarget.setBeingUnhooked(true);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // If survivor can't perform repairing anymore, we stop
        if (((Survivor) performer).isIncapacitated()) {
            end();
            return;
        }

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
        unhookProgress += UNHOOK_SPEED;
        if (unhookProgress >= MAX_UNHOOK_PROGRESS) {
            unhookTarget.getUnhooked(hook);
            end();
        }
    }

    @Override
    public void end() {
        super.end();
        ((Survivor) target).setBeingUnhooked(false);
    }

    @Override
    public float getProgress() {
        return unhookProgress / MAX_UNHOOK_PROGRESS;
    }
}

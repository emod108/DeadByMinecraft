package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;

public class WiggleAction extends Action {
    // Max wiggle progress
    private static final float MAX_WIGGLE_PROGRESS = 10f;

    // Wiggle progress achieved per tick
    public static final float WIGGLE_SPEED = 1f / Timings.TICKS_PER_SECOND;

    // How often can survivor toggle between trying and not trying to wiggle
    private static final int TOGGLE_BUFFER_MAX = Timings.secondsToTicks(0.5);

    // Current wiggle progress
    private float wiggleProgress = 0f;

    // If survivor isn't trying to wiggle, progress isn't accumulating
    private boolean tryingToWiggle = false;

    // How much time left, before survivor can toggle between trying and not trying to wiggle
    private int toggleBuffer = 0;

    public WiggleAction(final Survivor performer, final Killer carrier) {
        super(performer, carrier);
    }

    public void toggleTryingToWiggle() {
        if (toggleBuffer > 0)
            return;

        if (tryingToWiggle) {
            performer.getPlayer().sendMessage(ChatColor.YELLOW + "You aren't trying to escape " +
                    "the killer's grasp anymore");
        } else {
            performer.getPlayer().sendMessage(ChatColor.YELLOW + "You are now trying to escape " +
                    "the killer's grasp");
        }

        tryingToWiggle = !tryingToWiggle;
        toggleBuffer = TOGGLE_BUFFER_MAX;
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // If survivor is not being carried anymore, then we stop the action
        if (((Survivor) performer).getHealthState() != Survivor.HealthState.BEING_CARRIED) {
            end();
            return;
        }

        // The Killer has an action, very likely he's hooking this survivor or grabbing him
        // Can't wiggle at this moment
        if (((Killer) target).getAction() != null)
            return;

        // Decrementing toggle buffer
        if (toggleBuffer > 0)
            --toggleBuffer;

        // Survivor must try to wiggle to escape
        if (!tryingToWiggle)
            return;

        // Survivor manages to wiggle of
        wiggleProgress += WIGGLE_SPEED;
        if (wiggleProgress >= MAX_WIGGLE_PROGRESS) {
            ((Killer) target).getStunned();
            end();
        }
    }

    @Override
    public void end() {
        super.end();
    }

    @Override
    public float getProgress() {
        return wiggleProgress / MAX_WIGGLE_PROGRESS;
    }
}
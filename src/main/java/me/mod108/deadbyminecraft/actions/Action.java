package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.Target;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Prop;
import me.mod108.deadbyminecraft.utility.ActionBar;
import me.mod108.deadbyminecraft.utility.ProgressBar;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public abstract class Action extends BukkitRunnable {
    // How much progress an action gains per tick
    public static final float ACTION_SPEED = 1.0f / Timings.TICKS_PER_SECOND;

    // The one who performs the action
    protected final Character performer;

    // Target is being influenced by the action
    protected final Target target;

    // If true, action ends
    protected boolean cancelled = false;

    public Action(final Character performer, final Target target) {
        this.performer = performer;
        this.target = target;

        // Stopping any player movement
        performer.getPlayer().setVelocity(new Vector(0, 0, 0));

        // If it's a prop, we show that it's being interacted with
        if (target instanceof final Prop prop)
            prop.setInteractingPlayer(performer);
    }

    // Starts executing this action every tick
    public void run() {
        ProgressBar.setProgress(performer.getPlayer(), getProgress());
        ActionBar.setActionBar(performer.getPlayer(), getActionBar());
    }

    // Finishes executing the action
    public void end() {
        cancelled = true;
        cancel();
        performer.setAction(null);

        ProgressBar.resetProgress(performer.getPlayer());
        ActionBar.resetActionBar(performer.getPlayer());

        // If it's a prop, we show that it's now being free to be interacted with
        if (target instanceof final Prop prop)
            prop.setInteractingPlayer(null);
    }

    // Returns true if this action can be interrupted (because of an attack or a stun)
    public boolean isInterruptible() {
        return true;
    }

    // Finishes executing this action if it's interruptible
    public void interrupt() {
        if (isInterruptible())
            end();
    }

    // Gets current action progress in range from 0.0 to 1.0
    public abstract float getProgress();

    // Returns action bar message
    public abstract String getActionBar();

    public Character getPerformer() {
        return performer;
    }

    public Target getTarget() {
        return target;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}

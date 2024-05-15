package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.Target;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.utility.ProgressBar;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public abstract class Action extends BukkitRunnable {
    protected final Character performer;
    protected final Target target;

    // If true, action ends
    protected boolean cancelled = false;

    public Action(final Character performer, final Target target) {
        this.performer = performer;
        this.target = target;

        // Stopping any player movement
        performer.getPlayer().setVelocity(new Vector(0, 0, 0));
    }

    // Starts executing this action every tick
    public void run() {
        ProgressBar.setProgress(performer.getPlayer(), getProgress());
    }

    // Finishes executing the action
    public void end() {
        cancelled = true;
        cancel();
        performer.setAction(null);

        ProgressBar.resetProgress(performer.getPlayer());
    }

    // Finishes executing this action if it's interruptible
    public void interrupt() {
        if (isInterruptible())
            end();
    }

    // Gets current action progress in range from 0.0 to 1.0
    public abstract float getProgress();

    public Character getPerformer() {
        return performer;
    }

    public Target getTarget() {
        return target;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    // Returns trie if this action can be interrupted (because of an attack or a stun)
    public boolean isInterruptible() {
        return true;
    }
}

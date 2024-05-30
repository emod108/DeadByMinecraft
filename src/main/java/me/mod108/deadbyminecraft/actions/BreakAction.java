package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Breakable;
import org.bukkit.Sound;

public class BreakAction extends Action {
    // Max break progress
    private final float maxBreakProgress;

    // Sounds played when taking break action
    private final Sound breakingSound;

    // Current break progress
    private float breakProgress = 0.0f;

    // Variable needed to play breaking sounds when the progress reached 50%
    private boolean halfwayThrough = false;

    public BreakAction(final Killer performer, final Breakable breakable) {
        super(performer, breakable);

        maxBreakProgress = breakable.getBreakingTime();
        breakingSound = breakable.getBreakingSound();

        DeadByMinecraft.getPlugin().freezeManager.freeze(performer.getPlayer());
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Progressing the action
        breakProgress += ACTION_SPEED;

        // Playing break sound if breaking progress reaches half
        if (!halfwayThrough && breakProgress >= maxBreakProgress / 2) {
            halfwayThrough = true;
            SoundManager.playForAll(target.getLocation(), breakingSound, 1f, 1f);
        }

        // Breaking object, when the progress reaches max
        if (breakProgress >= maxBreakProgress) {
            end();
            SoundManager.playForAll(target.getLocation(), breakingSound, 1f, 1f);

            // Applying break action to the object
            final Breakable breakable = (Breakable) target;
            breakable.getBroken();
        }
    }

    @Override
    public void end() {
        super.end();
        DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer());
    }

    @Override
    public float getProgress() {
        return breakProgress / maxBreakProgress;
    }
}

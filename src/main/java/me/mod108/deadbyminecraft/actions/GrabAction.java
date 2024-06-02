package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Generator;
import me.mod108.deadbyminecraft.utility.ActionBar;
import me.mod108.deadbyminecraft.utility.ProgressBar;
import org.bukkit.ChatColor;

public class GrabAction extends Action {
    // Max grabbing progress
    private static final float MAX_GRABBING_PROGRESS = 1.5f;

    // Current action progress
    private float grabProgress = 0f;

    public GrabAction(final Killer performer, final Survivor target) {
        super(performer, target);
        DeadByMinecraft.getPlugin().freezeManager.freeze(performer.getPlayer().getUniqueId(), true);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Showing grabbing progress to survivor
        final Survivor survivor = (Survivor) target;
        ProgressBar.setProgress(survivor.getPlayer(), getProgress());
        ActionBar.setActionBar(survivor.getPlayer(), ChatColor.RED + "You are grabbed!");

        // Progressing the action
        grabProgress += ACTION_SPEED;

        // Finishing the action, when reached max progress
        if (grabProgress >= MAX_GRABBING_PROGRESS) {
            end();
        }
    }

    @Override
    public void end() {
        super.end();
        DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer().getUniqueId());

        // Resetting progress bar for survivor
        final Survivor survivor = (Survivor) target;
        ProgressBar.resetProgress(survivor.getPlayer());
        ActionBar.resetActionBar(survivor.getPlayer());
    }

    @Override
    public float getProgress() {
        return grabProgress / MAX_GRABBING_PROGRESS;
    }

    @Override
    public String getActionBar() {
        return ChatColor.YELLOW + "Grabbing survivor";
    }
}

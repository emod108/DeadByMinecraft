package me.mod108.deadbyminecraft.actions;

import me.mod108.crawlingplugin.CrawlingPlugin;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.FreezeManager;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.utility.ProgressBar;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;

public class PickUpAction extends Action {
    // Max picking up progress
    private static final float MAX_PICK_UP_PROGRESS = 3f;

    // Picking up progress achieved per tick
    private static final float PICK_UP_SPEED = 1.0f / Timings.TICKS_PER_SECOND;

    // Current picking up progress
    private float pickingUpProgress = 0f;

    public PickUpAction(final Killer killer, final Survivor survivor) {
        super(killer, survivor);

        killer.getPlayer().sendMessage(ChatColor.YELLOW + "Picking up survivor");
        survivor.getPlayer().sendMessage(ChatColor.YELLOW + "You are being picked up by the killer");
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        final Survivor survivor = (Survivor) target;

        // Showing pick up progress to survivor
        ProgressBar.setProgress(survivor.getPlayer(), getProgress());

        // Progressing the action
        pickingUpProgress += PICK_UP_SPEED;
        if (pickingUpProgress >= MAX_PICK_UP_PROGRESS) {
            // Making survivor to stop crawling
            CrawlingPlugin.getPlugin().getCrawlingManager().stopCrawling(survivor.getPlayer());

            // Placing survivor on top of the killer
            ((Killer) performer).getSurvivorOnShoulder(survivor);
            performer.getPlayer().sendMessage(ChatColor.YELLOW + "Picked up survivor");
            end();
        }
    }

    @Override
    public void end() {
        super.end();

        final FreezeManager manager = DeadByMinecraft.getPlugin().freezeManager;
        final Survivor survivor = (Survivor) target;

        // Unfreezing both players
        manager.unFreeze(performer.getPlayer());
        manager.unFreeze(survivor.getPlayer());
        ProgressBar.resetProgress(survivor.getPlayer());
    }

    @Override
    public float getProgress() {
        return pickingUpProgress / MAX_PICK_UP_PROGRESS;
    }
}
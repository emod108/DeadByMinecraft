package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Hatch;
import org.bukkit.ChatColor;

public class HatchCloseAction extends Action {
    private static final float MAX_CLOSING_PROGRESS = 1.5f;
    private float closingProgress = 0f;

    public HatchCloseAction(final Killer performer, final Hatch target) {
        super(performer, target);
        DeadByMinecraft.getPlugin().freezeManager.freeze(performer.getPlayer().getUniqueId(), true);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        closingProgress += ACTION_SPEED;
        if (closingProgress >= MAX_CLOSING_PROGRESS) {
            end();
            ((Hatch) target).close();
        }
    }

    @Override
    public void end() {
        super.end();
        DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer().getUniqueId());
    }

    @Override
    public float getProgress() {
        return closingProgress / MAX_CLOSING_PROGRESS;
    }

    @Override
    public String getActionBar() {
        return ChatColor.YELLOW + "Closing the hatch";
    }
}

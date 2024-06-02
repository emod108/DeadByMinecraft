package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Hook;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class HookAction extends Action {
    // Max hooking progress
    private static final float MAX_HOOKING_PROGRESS = 1f;

    // Current hooking progress
    private float hookingProgress = 0f;

    // Hook on which survivor will be hooked upon
    private final Hook hook;

    // Variable needed to play hooking sounds when the progress reached 50%
    private boolean halfwayThrough = false;

    public HookAction(final Killer killer, final Survivor survivor, final Hook hook) {
        super(killer, survivor);
        this.hook = hook;
        DeadByMinecraft.getPlugin().freezeManager.freeze(killer.getPlayer().getUniqueId(), true);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Playing hook sound if hooking progress reaches half
        if (!halfwayThrough && hookingProgress >= MAX_HOOKING_PROGRESS / 2) {
            halfwayThrough = true;
            SoundManager.playForAll(hook.getHook().getLocation(), Sound.ITEM_AXE_STRIP, 1, 1);
        }

        // Progressing the action
        hookingProgress += ACTION_SPEED;
        if (hookingProgress >= MAX_HOOKING_PROGRESS) {
            ((Killer) performer).stopCarrying();
            ((Survivor) target).getHooked(hook);
            end();
        }
    }

    @Override
    public void end() {
        super.end();
        DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer().getUniqueId());
    }

    @Override
    public float getProgress() {
        return hookingProgress / MAX_HOOKING_PROGRESS;
    }

    @Override
    public String getActionBar() {
        return ChatColor.YELLOW + "Hooking";
    }
}

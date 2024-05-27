package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Hook;
import me.mod108.deadbyminecraft.utility.Timings;

public class HookAction extends Action {
    // Max hooking progress
    private static final float MAX_HOOKING_PROGRESS = 1f;

    // Hooking progress achieved per tick
    private static final float HOOKING_SPEED = 1f / Timings.TICKS_PER_SECOND;

    // Current hooking progress
    private float hookingProgress = 0f;

    // Hook on which survivor will be hooked upon
    private final Hook hook;

    public HookAction(final Killer killer, final Survivor survivor, final Hook hook) {
        super(killer, survivor);
        this.hook = hook;
        DeadByMinecraft.getPlugin().freezeManager.freeze(killer.getPlayer());
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Progressing the action
        hookingProgress += HOOKING_SPEED;
        if (hookingProgress >= MAX_HOOKING_PROGRESS) {
            ((Killer) performer).stopCarrying();
            ((Survivor) target).getHooked(hook);
            end();
        }
    }

    @Override
    public void end() {
        super.end();
        DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer());
    }

    @Override
    public float getProgress() {
        return hookingProgress / MAX_HOOKING_PROGRESS;
    }
}

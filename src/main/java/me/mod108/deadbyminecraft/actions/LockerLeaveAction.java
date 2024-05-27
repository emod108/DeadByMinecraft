package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.props.Locker;

public class LockerLeaveAction extends LockerAction {
    public LockerLeaveAction(final Survivor performer, final Locker target,
                             final int actionTimeTicks, final boolean isRushed) {
        super(performer, target, actionTimeTicks, isRushed);
        performer.teleportFromLocker(target);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // If survivor is unable to continue the action, we return
        final Survivor survivor = (Survivor) performer;
        if (survivor.isIncapacitated()) {
            end();
            return;
        }

        // Progressing the action
        ++currentActionTime;

        // Action was finished
        if (currentActionTime >= actionTimeTicks) {
            end();
        }
    }

    @Override
    public void end() {
        super.end();

        // Unfreezing survivor
        DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer());
    }
}

package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.props.Locker;

public class LockerEnterAction extends LockerAction {
    public LockerEnterAction(final Survivor performer, final Locker target,
                             final int actionTimeTicks, final boolean isRushed) {
        super(performer, target, actionTimeTicks, isRushed);
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
            survivor.teleportToLocker((Locker) target);
            end();
        }
    }

    @Override
    public void end() {
        super.end();

        // If survivor didn't succeed in entering the locker
        if (performer.getMovementState() != Character.MovementState.IN_LOCKER)
            DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer());
    }
}

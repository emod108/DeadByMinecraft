package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Locker;

public class LockerAction extends Action {
    // For how long this action must go
    protected final int actionTimeTicks;

    // For how long this action goes already
    protected int currentActionTime = 0;

    // Indicates if it's a rushed locker action. Killer searching a locker always considers to be rushed
    protected final boolean isRushed;

    public LockerAction(final Character performer, final Locker target,
                        final int actionTimeTicks, final boolean isRushed) {
        super(performer, target);

        // Freezing player
        DeadByMinecraft.getPlugin().freezeManager.freeze(performer.getPlayer());

        this.actionTimeTicks = actionTimeTicks;
        this.isRushed = isRushed;

        if (isRushed)
            SoundManager.playForAll(target.getLocation(), Locker.ENTER_SOUND, 1f, 1f);
    }

    @Override
    public void end() {
        super.end();
        if (isRushed)
            SoundManager.playForAll(target.getLocation(), Locker.LEAVE_SOUND, 1f, 1f);
    }

    @Override
    public float getProgress() {
        return (float) currentActionTime / actionTimeTicks;
    }
}

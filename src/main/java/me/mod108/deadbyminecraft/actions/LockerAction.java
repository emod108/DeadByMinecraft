package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Locker;

public abstract class LockerAction extends Action {
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
        final boolean freezeCamera = performer instanceof Killer;
        DeadByMinecraft.getPlugin().freezeManager.freeze(performer.getPlayer().getUniqueId(), freezeCamera);

        this.actionTimeTicks = actionTimeTicks;
        this.isRushed = isRushed;

        target.openDoor(isRushed);
    }

    @Override
    public void end() {
        super.end();
        ((Locker) target).closeDoor(isRushed);
    }

    @Override
    public float getProgress() {
        return (float) currentActionTime / actionTimeTicks;
    }
}

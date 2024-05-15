package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.FreezeManager;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.utility.Timings;

public class PalletBreakAction extends Action {
    // Pallet break progress achieved per second
    private static final float PALLET_BREAK_SPEED = 1.0f / Timings.TICKS_PER_SECOND;

    // Max pallet break progress
    private static final float MAX_PALLET_BREAK_PROGRESS = 2.3f;

    // Current pallet break progress
    private float palletBreakProgress = 0.0f;

    // Stopping killer movements while he is doing an action
    private final FreezeManager manager = DeadByMinecraft.getPlugin().freezeManager;

    // Variable needed to play breaking sounds when the progress reached 50%
    private boolean halfwayThrough = false;

    public PalletBreakAction(final Killer performer, final Pallet pallet) {
        super(performer, pallet);
        pallet.startDestroying();
        manager.freeze(performer.getPlayer());
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        final Pallet pallet = (Pallet) target;

        if (!halfwayThrough && palletBreakProgress >= MAX_PALLET_BREAK_PROGRESS / 2) {
            SoundManager.playForAll(pallet.getLocation(), Pallet.BREAK_SOUND, 1.0f, 1.0f);
            halfwayThrough = true;
        }

        // Unhook when progress reaches maximum
        palletBreakProgress += PALLET_BREAK_SPEED;
        if (palletBreakProgress >= MAX_PALLET_BREAK_PROGRESS) {
            SoundManager.playForAll(pallet.getLocation(), Pallet.BREAK_SOUND, 1.0f, 1.0f);
            pallet.destroy();
            end();
        }
    }

    @Override
    public void end() {
        super.end();
        manager.unFreeze(performer.getPlayer());
    }

    @Override
    public float getProgress() {
        return palletBreakProgress / MAX_PALLET_BREAK_PROGRESS;
    }
}

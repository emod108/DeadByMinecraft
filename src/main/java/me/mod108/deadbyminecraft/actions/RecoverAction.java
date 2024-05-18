package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.utility.ProgressBar;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class RecoverAction extends Action {
    // Recovery progress achieved per tick (2 times slower than healing)
    private static final float RECOVER_SPEED = HealAction.HEALING_SPEED / 2.0f;
    private static final float MAX_RECOVER_PROGRESS = Survivor.MAX_HEALING_PROGRESS * 0.95f;

    // If players moves, recovering process stops
    private Location startLocation = null;

    public RecoverAction(final Survivor recovering) {
        super(recovering, recovering);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Getting starting location
        if (startLocation == null)
            startLocation = performer.getLocation();

        // Checking if survivor moved
        final Location currentLocation = performer.getLocation();
        if (currentLocation.getX() != startLocation.getX() || currentLocation.getZ() != startLocation.getZ() ||
                currentLocation.getY() != startLocation.getY()) {
            performer.getPlayer().sendMessage(ChatColor.RED + "Recovery process stopped because you moved!");
            end();
            return;
        }

        // Check if survivor is able to recover
        final Survivor survivor = (Survivor) performer;
        if (survivor.isBeingHealed() || survivor.getHealthState() != Survivor.HealthState.DYING) {
            end();
            return;
        }

        // Add recovery progress up to MAX_RECOVER_PROGRESS
        final float currentProgress = survivor.getHealingProgress();
        float recoveredHealth = RECOVER_SPEED;
        if (currentProgress + recoveredHealth >= MAX_RECOVER_PROGRESS)
            recoveredHealth -= currentProgress + recoveredHealth - MAX_RECOVER_PROGRESS;
        survivor.addHealingProgress(recoveredHealth);
    }

    @Override
    public void end() {
        super.end();

        final Survivor survivor = (Survivor) performer;
        if (survivor.getHealthState() == Survivor.HealthState.DYING)
            ProgressBar.setProgress(survivor.getPlayer(), getProgress());
    }

    @Override
    public float getProgress() {
        return ((Survivor) performer).getHealingProgressPercents();
    }
}

package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import org.bukkit.ChatColor;

public class SelfUnhookAction extends Action {
    // Max unhook progress
    private static final float MAX_UNHOOK_PROGRESS = 1.5f;

    // Current unhook progress
    private float unhookProgress = 0f;

    public SelfUnhookAction(final Survivor survivor) {
        super(survivor, survivor);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        final Survivor survivor = (Survivor) performer;

        // Checking if survivor can try to self-unhook
        if (!survivor.canSelfUnhook()) {
            end();
            return;
        }

        // Survivor must be sneaking to try to self-unhook
        if (!survivor.getPlayer().isSneaking()) {
            end();
            return;
        }

        // Survivor tries to self-unhook
        unhookProgress += ACTION_SPEED;
        if (unhookProgress >= MAX_UNHOOK_PROGRESS) {
            survivor.trySelfUnhook();
            end();
        }
    }

    @Override
    public float getProgress() {
        return unhookProgress / MAX_UNHOOK_PROGRESS;
    }

    @Override
    public String getActionBar() {
        return ChatColor.GREEN + "Trying to self-unhook";
    }
}

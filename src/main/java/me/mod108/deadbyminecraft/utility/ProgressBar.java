package me.mod108.deadbyminecraft.utility;

import org.bukkit.entity.Player;

public class ProgressBar {
    // Sets action progress between 0 and 1
    public static void setProgress(final Player player, final float progress) {
        player.setExp(Math.min(Math.max(0.0f, progress), 1.0f));
    }

    public static void resetProgress(final Player player) {
        player.setExp(0.0f);
    }
}

package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropItemListener implements Listener {
    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent e) {
        // Checking if the game is going
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        // Checking if player is in the game
        final Character player = game.getPlayer(e.getPlayer());
        if (player == null)
            return;

        e.setCancelled(true);
    }
}

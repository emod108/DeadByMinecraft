package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        // Checking if the game is going
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        // Checking if player is in the game
        final Player player = (Player) e.getWhoClicked();
        if (game.getPlayer(player) == null)
            return;

        e.setCancelled(true);
    }
}

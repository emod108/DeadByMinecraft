package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        // Checking if the game is going
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        // It must be a player
        if (!(e.getEntity() instanceof final Player player))
            return;

        // Getting the player
        final Character character = game.getPlayer(player);

        // Player is not a part of the game
        if (character == null)
            return;

        // Cancelling the event if it's not custom damage
        if (e.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
            e.setCancelled(true);
        }
    }
}

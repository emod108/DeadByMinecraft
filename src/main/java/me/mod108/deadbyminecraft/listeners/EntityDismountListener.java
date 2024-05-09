package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

public class EntityDismountListener implements Listener {
    @EventHandler
    public void onDismount(final EntityDismountEvent e) {
        // Checking if the game is going
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        // Checking if entity is a player
        final Entity entity = e.getEntity();
        if (!(entity instanceof final Player player))
            return;

        // Getting the killer
        final Killer killer = game.getKiller();
        if (killer == null)
            return;

        // Checking if killer carries someone
        final Survivor carriedSurvivor = killer.getCarriedSurvivor();
        if (carriedSurvivor == null)
            return;

        // Checking if killer carries this player
        if (carriedSurvivor.getPlayer().getUniqueId().equals(player.getUniqueId()))
            e.setCancelled(true);
    }
}

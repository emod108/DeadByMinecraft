package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.events.KillerAttackSurvivorEvent;
import me.mod108.deadbyminecraft.events.KillerMissEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityDamageEntityListener implements Listener {
    private final Set<UUID> attackers = new HashSet<>();

    @EventHandler
    public void onEntityDamageEntity(final EntityDamageByEntityEvent e) {
        // Checking if the game is going
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        // Attacker must be a player
        if (!(e.getDamager() instanceof final Player attacker))
            return;

        // Victim must be a player
        if (!(e.getEntity() instanceof final Player victim))
            return;

        // Cancelling event because we are handling it our way
        e.setCancelled(true);

        // Getting the killer
        final Character attackerPlayer = game.getPlayer(attacker);

        // Player is not a part of the game
        if (attackerPlayer == null)
            return;

        // Not the killer
        if (!(attackerPlayer instanceof final Killer killer))
            return;

        // Adding killer to the list of attackers for later use
        attackers.add(attacker.getUniqueId());

        // Getting the survivor
        final Character victimPlayer = game.getPlayer(victim);

        // Player is not a part of the game
        if (victimPlayer == null)
            return;

        // Not a survivor
        if (!(victimPlayer instanceof final Survivor survivor))
            return;

        Bukkit.getServer().getPluginManager().callEvent(new KillerAttackSurvivorEvent(killer, survivor));
    }

    // This event fires every time players left clicks
    // Needed to check later if the killer missed an attack
    @EventHandler
    public void onSwinging(final PlayerInteractEvent e) {
        // If it's not a left click, we return
        final Action playerAction = e.getAction();
        if (playerAction != Action.LEFT_CLICK_AIR && playerAction != Action.LEFT_CLICK_BLOCK)
            return;

        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        final Player player = e.getPlayer();
        final BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                // Killer attacked someone, it's already handled
                if (attackers.contains(player.getUniqueId())) {
                    attackers.remove(player.getUniqueId());
                    return;
                }

                // Checking game
                final Game game = DeadByMinecraft.getPlugin().getGame();
                if (game == null)
                    return;

                // Player must be in the game
                final Character attacker = game.getPlayer(player);
                if (attacker == null)
                    return;

                // Player must be the killer
                if (attacker instanceof final Killer killer)
                    Bukkit.getServer().getPluginManager().callEvent(new KillerMissEvent(killer));
            }
        }, 1L);
    }
}

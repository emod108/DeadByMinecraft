package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.events.KillerPickupSurvivorEvent;
import me.mod108.deadbyminecraft.events.SurvivorUnhookEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractEntityListener implements Listener {
    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        // Only right-clicking is allowed
        if (e.getHand() != EquipmentSlot.OFF_HAND)
            return;

        final Entity entity = e.getRightClicked();
        if (!(entity instanceof final Player clickedPlayer))
            return;

        // The game must be going
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        final Player player = e.getPlayer();

        // Interacting player must be in the game
        final Character interactingPlayer = game.getPlayer(player);
        if (interactingPlayer == null)
            return;

        // Player, who is being interacted with, must be in the game
        final Character interactedWithPlayer = game.getPlayer(clickedPlayer);
        if (interactedWithPlayer == null)
            return;

        // Killer interacting with a survivor
        if (interactingPlayer instanceof final Killer killer) {
            if (interactedWithPlayer instanceof final Survivor survivor) {
                // Killer picking a survivor up
                if (survivor.getHealthState() == Survivor.HealthState.DYING) {
                    final KillerPickupSurvivorEvent event = new KillerPickupSurvivorEvent(killer, survivor);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                }
            }
        }

        // A survivor interacting with another survivor
        if (interactingPlayer instanceof final Survivor performer) {
            if (interactedWithPlayer instanceof final Survivor target) {
                // Survivor unhooks another survivor
                if (target.getHealthState() == Survivor.HealthState.HOOKED) {
                    final SurvivorUnhookEvent event = new SurvivorUnhookEvent(performer, target);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                }
            }
        }
    }
}

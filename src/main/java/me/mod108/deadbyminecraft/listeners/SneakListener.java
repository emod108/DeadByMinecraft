package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.actions.Action;
import me.mod108.deadbyminecraft.actions.SelfUnhookAction;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SneakListener implements Listener {
    @EventHandler
    public void onPlayerSneak(final PlayerToggleSneakEvent e) {
        // Checking if the game is going
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        final Game game = plugin.getGame();
        if (game == null)
            return;

        // Checking if player is in the game
        final Player p = e.getPlayer();
        final Character player = game.getPlayer(p);
        if (player == null)
            return;

        // Checking if it's a survivor
        if (!(player instanceof final Survivor survivor))
            return;

        if (!e.isSneaking())
            return;

        if (survivor.isBeingUnhooked())
            return;

        // Survivor starts trying to self-unhook
        if (survivor.getHealthState() == Survivor.HealthState.HOOKED && survivor.canSelfUnhook()) {
            final Action unhookAction = new SelfUnhookAction(survivor);
            survivor.setAction(unhookAction);
            unhookAction.runTaskTimer(plugin, 0, 1);
        }
    }
}

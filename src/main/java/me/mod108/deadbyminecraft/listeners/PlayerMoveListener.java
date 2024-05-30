package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.utility.EscapeLine;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

// This listener checks if players have escaped
public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        // Getting the game
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        // Getting player
        final Player player = e.getPlayer();

        // We don't check spectators
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;

        // Checking if this player is in the game
        final Character character = game.getPlayer(player);
        if (character == null)
            return;

        final ArrayList<EscapeLine> escapes = game.getEscapeLines();

        // Checking if survivors escaped
        if (character instanceof final Survivor survivor) {
            for (final EscapeLine escape : escapes) {
                if (escape.hasCrossedIt(survivor)) {
                    survivor.escape();
                    return;
                }
            }
            return;
        }

        // Checking if we need to visually block the exit for the killer
        final Killer killer = (Killer) character;
        for (final EscapeLine escape : escapes) {
            if (escape.isVisibleToKiller() && escape.getDistanceToKiller(killer) > EscapeLine.BLOCKER_VISIBLE_DISTANCE) {
                escape.hideFromKiller(killer);
            } else if (!escape.isVisibleToKiller() && escape.getDistanceToKiller(killer) < EscapeLine.BLOCKER_VISIBLE_DISTANCE) {
                escape.showToKiller(killer);
            }
        }
    }
}

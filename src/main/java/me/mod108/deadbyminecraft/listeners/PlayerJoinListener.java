package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

// Is called whenever a player who left mid-game comes back
public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
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

        // Refreshing player instance in Character object
        player.setPlayer(p);

        // Players who joined back are considered dead
        p.setGameMode(GameMode.SPECTATOR);
    }
}

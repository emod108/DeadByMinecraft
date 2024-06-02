package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Hook;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

// Is called whenever player leaves the server
// Needed to correctly handle situations when players leave mid-game
public class PlayerLeaveListener implements Listener {
    @EventHandler
    public void onPlayerLeave(final PlayerQuitEvent e) {
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

        if (player instanceof final Killer killer) {
            e.setQuitMessage(ChatColor.RED + "Killer " + p.getName() + " has left the game!\nGame was ended");
            plugin.finishGame();
        } else if (player instanceof final Survivor survivor) {
            e.setQuitMessage(ChatColor.RED + "Survivor " + p.getName() + " has left the game!");

            // If survivor was on hook while he disconnected, it counts as sacrifice
            final Hook hook = survivor.getHook();
            if (hook != null) {
                hook.becomeBroken();
            }

            game.resetPlayer(player);
            survivor.setHealthState(Survivor.HealthState.DISCONNECTED);
        }
    }
}

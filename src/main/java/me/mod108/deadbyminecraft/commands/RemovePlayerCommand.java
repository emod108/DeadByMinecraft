package me.mod108.deadbyminecraft.commands;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.utility.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// This command removes a player from a lobby
public class RemovePlayerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        if (plugin.getGame() != null) {
            sender.sendMessage(ChatColor.YELLOW + "The game has started already! Now it isn't " +
                    "possible to remove a player.");
            return true;
        }

        final Lobby lobby = plugin.getLobby();
        if (lobby == null) {
            sender.sendMessage(ChatColor.YELLOW + "There's no lobby to remove the player from!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "No username provided! If you want to remove a player, " +
                    "then provide his username");
            return true;
        }

        // args[0] - player username
        final Player player = plugin.getServer().getPlayerExact(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "There's no player named \"" + args[0] + "\"");
            return true;
        }

        if (lobby.hasPlayer(player)) {
            lobby.removePlayer(player);
            sender.sendMessage(ChatColor.GREEN + "Player was removed successfully!");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "This player isn't present in the lobby!");
        }


        return true;
    }
}

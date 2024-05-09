package me.mod108.deadbyminecraft.commands;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// This command cancels the game by deleting the lobby
public class DeleteLobbyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        if (plugin.getLobby() != null) {
            plugin.setLobby(null);
            sender.sendMessage(ChatColor.GREEN + "Lobby was deleted successfully!");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "There's no lobby to delete!");
        }

        return true;
    }
}

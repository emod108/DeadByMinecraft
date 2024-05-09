package me.mod108.deadbyminecraft.commands;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.utility.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// This command is used to create a game lobby
public class CreateLobbyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        if (plugin.getLobby() != null)
            sender.sendMessage(ChatColor.YELLOW + "Lobby already exists! Try to use the existing one.");
        else if (plugin.getGame() != null)
            sender.sendMessage(ChatColor.YELLOW + "Game is already going on! Finish it to create another one.");
        else {
            plugin.setLobby(new Lobby());
            sender.sendMessage(ChatColor.GREEN + "Lobby has been created successfully!");
        }

        return true;
    }
}

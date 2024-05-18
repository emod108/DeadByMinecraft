package me.mod108.deadbyminecraft.commands;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// This command starts the game
public class StartGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        if (plugin.getGame() != null) {
            sender.sendMessage(ChatColor.YELLOW + "There's a game going already! Finish this game first.");
            return true;
        } else if (plugin.getLobby() == null) {
            sender.sendMessage(ChatColor.YELLOW + "No lobby exists to get players from! Create a lobby first.");
        } else {
            plugin.startGame();
            sender.sendMessage(ChatColor.GREEN + "The game was started successfully!");
        }

        return true;
    }
}

package me.mod108.deadbyminecraft.commands;

import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// This command prints the list of all available killers
public class GetKillersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String delimiter = ", ";
        final String output = String.join(delimiter, Killer.KILLER_NAMES);

        sender.sendMessage(ChatColor.RED + output);
        return true;
    }
}

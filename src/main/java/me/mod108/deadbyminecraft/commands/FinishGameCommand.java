package me.mod108.deadbyminecraft.commands;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// This command finishes the game before its natural ending
public class FinishGameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        final Game game = plugin.getGame();

        if (game == null) {
            sender.sendMessage(ChatColor.YELLOW + "There's no game to finish!");
        } else {
            game.finishGame();
            plugin.setGame(null);
            sender.sendMessage(ChatColor.GREEN + "Game was finished successfully!");
        }

        return true;
    }
}

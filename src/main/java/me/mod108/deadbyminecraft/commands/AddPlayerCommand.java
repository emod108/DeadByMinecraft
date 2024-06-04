package me.mod108.deadbyminecraft.commands;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.utility.Game;
import me.mod108.deadbyminecraft.utility.Lobby;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.characters.killers.Trapper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// This command adds players to a game lobby
public class AddPlayerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        // Game must not be running
        if (plugin.getGame() != null) {
            sender.sendMessage(ChatColor.YELLOW + "A game is going already! " +
                    "Adding new players is not possible.");
            return true;
        }

        // Lobby must exist
        final Lobby lobby = plugin.getLobby();
        if (lobby == null) {
            sender.sendMessage(ChatColor.YELLOW + "There's no lobby to add any players! " +
                    "Create a lobby to start adding players.");
            return true;
        }

        // Check args[] size. It must contain at least 2 elements and 3 if it's a killer
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Not enough arguments provided!\n" +
                    "Usage example: /" + label + " (username) (role) [which killer if not survivor]");
            return true;
        }

        // args[0] - player username
        final Player player = plugin.getServer().getPlayerExact(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "There's no player named \"" + args[0] + "\"");
            return true;
        }

        // args[1] - player role (Survivor or Killer)
        // Adding survivor
        if (args[1].equalsIgnoreCase("SURVIVOR")) {
            // Checking if there's still a place for survivors
            if (lobby.hasMaxSurvivors()) {
                sender.sendMessage(ChatColor.YELLOW+ "There are already " + Game.MAX_SURVIVORS_NUM
                        + " Survivors! You can't add anymore until you remove someone.");
                return true;
            }

            lobby.addSurvivor(player);
            sender.sendMessage(ChatColor.GREEN + "Added new survivor.");
        // Adding killer
        } else if (args[1].equalsIgnoreCase("Killer")) {
            // Checking if killer type was chosen
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED+ "No killer provided!\n" +
                        "Usage example: /" + label + " (username) (role) [which if not survivor]\n" +
                        "Use /getkillers to get all possible killers to choose from");
                return true;
            }

            // arg[2] - which killer
            final String whichKiller = args[2];
            final Killer killer;
            if (whichKiller.equalsIgnoreCase("TRAPPER")) {
                killer = new Trapper(player);
            } else {
                sender.sendMessage(ChatColor.RED + "Couldn't find killer " + args[2] + "!\n" +
                        "Use /getkillers to get all possible killers to choose from");
                return true;
            }

            // Checking if lobby has a killer already
            if (lobby.hasKiller()) {
                lobby.removeKiller();
                sender.sendMessage(ChatColor.YELLOW + "There was already a killer chosen!" +
                        "Previous killer was removed.");
            }

            lobby.addKiller(killer);
            sender.sendMessage(ChatColor.GREEN + "Added new killer.");
        }
        else {
            sender.sendMessage(ChatColor.RED+ "No roles provided! You need to specify if it's" +
                    "Survivor or Killer\n" +
                    "Usage example: /" + label + " (username) (role) [which if not survivor]");
        }

        return true;
    }
}

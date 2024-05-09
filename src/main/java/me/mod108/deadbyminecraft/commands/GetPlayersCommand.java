package me.mod108.deadbyminecraft.commands;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

// This command prints the list of all players in the game or in the lobby
public class GetPlayersCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        // Checking if there's a game or a lobby
        final ArrayList<Character> players;
        if (plugin.getGame() != null) {
            players = plugin.getGame().getPlayers();
        } else if (plugin.getLobby() != null) {
            players = plugin.getLobby().getPlayers();
        } else {
            sender.sendMessage(ChatColor.RED + "There is no game or lobby created!");
            return true;
        }

        // If there are no players
        if (players.size() == 0) {
            final String where = (plugin.getGame() == null) ? " lobby!" : " game!";
            sender.sendMessage(ChatColor.GREEN + "Currently there are no players in the" + where);
            return true;
        }

        // Making the list
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GREEN);
        for (final Character player : players) {
            builder.append(player.getPlayer().getName());
            final String role = (player instanceof Survivor) ? " (Survivor), " : " (Killer), ";
            builder.append(role);
        }

        // Removing the last ", " sequence
        builder.delete(builder.length() - 2, builder.length());

        // Sending the list of players
        sender.sendMessage(builder.toString());

        return true;
    }
}

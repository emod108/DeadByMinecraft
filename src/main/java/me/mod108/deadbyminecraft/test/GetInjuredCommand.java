package me.mod108.deadbyminecraft.test;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetInjuredCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof final Player player))
            return true;

        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        final Game game = plugin.getGame();
        if (game == null) {
            player.sendMessage("Game isn't running!");
            return true;
        }

        final Character character = game.getPlayer(player);
        if (character == null) {
            player.sendMessage("You're not in the game!");
            return true;
        }

        if (!(character instanceof final Survivor survivor)) {
            player.sendMessage("You're not a survivor!");
            return true;
        }

        survivor.getHit();
        return true;
    }
}

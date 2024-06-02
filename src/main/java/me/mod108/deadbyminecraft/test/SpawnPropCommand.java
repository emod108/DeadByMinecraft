package me.mod108.deadbyminecraft.test;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.props.*;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.targets.props.vaultable.Window;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnPropCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof final Player player))
            return true;

        if (args.length < 1) {
            player.sendMessage("No arguments provided!");
            return true;
        }

        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        final Game game = plugin.getGame();
        if (game == null) {
            player.sendMessage("Game isn't running!");
            return true;
        }

        final String propToSpawn = args[0];
        final Prop prop;
        switch (propToSpawn.toUpperCase()) {
            case "LOCKER" -> prop = new Locker(player.getLocation(), BlockFace.NORTH);
            case "WINDOW" -> prop = new Window(player.getLocation(), Material.STONE_BRICK_WALL);
            case "PALLET" -> prop = new Pallet(player.getLocation(), BlockFace.NORTH);
            case "GENERATOR" -> prop = new Generator(player.getLocation(), BlockFace.NORTH,
                    Generator.GeneratorType.OUTDOOR);
            case "EXITGATE" -> prop = new ExitGate(player.getLocation(), BlockFace.NORTH);
            case "HOOK" -> prop = new Hook(player.getLocation(), BlockFace.NORTH);
            case "HATCH" -> prop = new Hatch(player.getLocation(), BlockFace.NORTH);
            default -> {
                player.sendMessage("No prop was chosen!");
                return true;
            }
        }

        game.addProp(prop);
        return true;
    }
}

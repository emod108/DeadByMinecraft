package me.mod108.deadbyminecraft.test;

import me.mod108.deadbyminecraft.utility.MovementSpeed;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMovementSpeedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof final Player player) {
            // If no speed value was provided
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "No speed value provided! " +
                        "Specify a value from 0.0 to 50.0");
                return true;
            }

            // Parsing speed value
            final float speed;
            try {
                speed = Float.parseFloat(args[0]);
            } catch (final NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid speed value! " +
                        "Specify a value from 0.0 to 50.0");
                return true;
            }

            // In case if the value is out of bounds
            if (speed < 0.0f || speed > 50.0f) {
                player.sendMessage(ChatColor.RED + "Speed value is out of bounds! " +
                        "Specify a value from 0.0 to 50.0");
                return true;
            }

            // Applying requested speed
            final MovementSpeed desiredSpeed = new MovementSpeed(speed);
            desiredSpeed.applyToPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Successfully applied new speed!");
        }
        return true;
    }
}

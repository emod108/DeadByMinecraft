package me.mod108.deadbyminecraft.test;

import me.mod108.deadbyminecraft.utility.MovementSpeed;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GetMovementSpeedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof final Player player) {
            // Getting current players speed
            final float spigotWalkSpeed = player.getWalkSpeed();
            final PotionEffect speedEffect = player.getPotionEffect(PotionEffectType.SPEED);
            final int speedEffectAmplifier = (speedEffect == null) ?
                    MovementSpeed.NO_SPEED_EFFECT_AMPLIFIER : speedEffect.getAmplifier();
            final float speed = MovementSpeed.calculateSpeed(spigotWalkSpeed, speedEffectAmplifier);

            // Printing the result
            player.sendMessage(ChatColor.YELLOW + "Your current movement speed is: "
                    + MovementSpeed.toPercents(speed));
        }
        return true;
    }
}

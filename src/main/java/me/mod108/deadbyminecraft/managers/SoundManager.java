package me.mod108.deadbyminecraft.managers;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;

public class SoundManager implements CommandExecutor {
    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
                             final String label, final String[] args) {
        if (sender instanceof final Player player) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.YELLOW + "No sound name provided!");
                return true;
            }

            try {
                final Sound sound = Sound.valueOf(args[0].toUpperCase());
                float volume = 1.0f;
                float pitch = 1.0f;
                if (args.length > 1)
                    volume = Float.parseFloat(args[1]);
                if (args.length > 2)
                    pitch = Float.parseFloat(args[2]);
                playForOne(player, player.getLocation(), sound, volume, pitch);
            }
            catch (final IllegalArgumentException e) {
                player.sendMessage(ChatColor.YELLOW + "No such sound was found!");
            }
        }

        return true;
    }

    // Plays a sound for a player at a location
    // If the location is null, plays the sound at the player's location
    public static void playForOne(final Player player, final Location location, final Sound sound,
                           final float volume, final float pitch) {
        player.playSound(Objects.requireNonNullElseGet(location, player::getLocation), sound, volume, pitch);
    }

    // Plays a sound for a group of players at a location
    // If the location is null, plays the sound at the player's location
    public static void playForGroup(final ArrayList<Player> players, final Location location, final Sound sound,
                             final float volume, final float pitch) {
        for (final Player player : players) {
            playForOne(player, location, sound, volume, pitch);
        }
    }

    // Plays a sound for ALL players at a location
    // If the location is null, plays the sound at the player's location
    public static void playForAll(final Location location, final Sound sound, final float volume, final float pitch) {
        for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
            playForOne(player, location, sound, volume, pitch);
        }
    }
}

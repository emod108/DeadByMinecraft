package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.KillerAttackSurvivorEvent;
import me.mod108.deadbyminecraft.events.KillerMissEvent;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KillerAttackSurvivorListener implements Listener {
    @EventHandler
    public void onKillerAttackSurvivor(final KillerAttackSurvivorEvent e) {
        final Killer killer = e.getKiller();
        final Survivor survivor = e.getSurvivor();

        // The survivor must be hittable
        if (!survivor.isHittable()) {
            final KillerMissEvent event = new KillerMissEvent(killer);
            Bukkit.getServer().getPluginManager().callEvent(event);
            return;
        }

        // Checking if killer is able to attack survivors
        if (!killer.canHitSurvivors()) {
            if (killer.isStunned()) { // Is stunned
                killer.getPlayer().sendMessage(ChatColor.RED + "You can't hit survivors while stunned. " +
                        "Stun time left: " + Timings.ticksToSeconds(killer.getStunTime()) + "s");
            }
            else if (killer.isOnAttackCooldown()) { // On attack cooldown
                killer.getPlayer().sendMessage(ChatColor.RED + "Your attack is still on cooldown. " +
                        "You can attack again in: " + Timings.ticksToSeconds(killer.getAttackCooldownTime()) + "s");
            } else { // Doing action
                killer.getPlayer().sendMessage(ChatColor.RED + "You can't hit survivors while doing actions.");
            }

            return;
        }

        // Checking attack distance
        final Location killerLocation = killer.getLocation();
        final Location survivorLocation = survivor.getLocation();
        final double attackDistance = killerLocation.distanceSquared(survivorLocation);
        if (attackDistance > Killer.ATTACK_DISTANCE_SQUARED) {
            killer.getPlayer().sendMessage(ChatColor.RED + "You must be closer to hit this survivor!");
            final KillerMissEvent event = new KillerMissEvent(killer);
            Bukkit.getServer().getPluginManager().callEvent(event);
            return;
        }

        // Killers can grab survivors instead of hitting them
        // Works only if survivors can be grabbed and the killer is not carrying anyone yet
        if (survivor.isGrabbable() && killer.getCarriedSurvivor() == null) {
            killer.grab(survivor);
            return;
        }

        // Hitting survivors
        killer.hit(survivor);
    }

    @EventHandler
    public void onKillerMiss(final KillerMissEvent e) {
        final Killer killer = e.getKiller();

        // Checking if killer is able to attack survivors
        if (!killer.canHitSurvivors()) {
            if (killer.isStunned()) { // Is stunned
                killer.getPlayer().sendMessage(ChatColor.RED + "You can't hit survivors while stunned. " +
                        "Stun time left: " + Timings.ticksToSeconds(killer.getStunTime()) + "s");
            }
            else if (killer.isOnAttackCooldown()) { // On attack cooldown
                killer.getPlayer().sendMessage(ChatColor.RED + "Your attack is still on cooldown. " +
                        "You can attack again in: " + Timings.ticksToSeconds(killer.getAttackCooldownTime()) + "s");
            } else { // Doing action
                killer.getPlayer().sendMessage(ChatColor.RED + "You can't hit survivors while doing actions.");
            }

            return;
        }

        // Killer missed
        killer.hit(null);
    }
}

package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.SurvivorHealEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SurvivorHealListener implements Listener {
    @EventHandler
    public void onSurvivorHeal(final SurvivorHealEvent e) {
        final Survivor performer = e.getPerformer();
        final Survivor healingTarget = e.getHealingTarget();

        // Ability check
        if (!performer.canInteractWithSurvivor())
            return;
        if (!healingTarget.isHealable())
            return;

        // Distance check
        final Location performerLocation = performer.getLocation();
        final Location healingTargetLocation = healingTarget.getLocation();
        final double healingDistance = performerLocation.distanceSquared(healingTargetLocation);
        if (healingDistance > Character.ACTION_DISTANCE_SQUARED)
            return;

        // Can't heal sneaking players
        if (healingTarget.getPlayer().isSneaking()) {
            performer.getPlayer().sendMessage(ChatColor.RED + "Target doesn't want to be healed.");
            return;
        }

        performer.startHealing(healingTarget);
    }
}

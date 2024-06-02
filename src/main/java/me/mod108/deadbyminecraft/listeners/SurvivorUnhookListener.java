package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.SurvivorUnhookEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SurvivorUnhookListener implements Listener {
    @EventHandler
    public void onSurvivorUnhook(final SurvivorUnhookEvent e) {
        final Survivor performer = e.getPerformer();
        final Survivor unhookTarget = e.getUnhookTarget();

        // Ability check
        if (!performer.canInteractWithSurvivor())
            return;

        // Hooked survivor can be unhooked only by one person at a time
        if (unhookTarget.isBeingUnhooked())
            return;

        // Distance check
        final Location performerLocation = performer.getLocation();
        final Location unhookTargetLocation = unhookTarget.getLocation();
        final double unhookDistance = performerLocation.distanceSquared(unhookTargetLocation);
        if (unhookDistance > Character.ACTION_DISTANCE_SQUARED) {
            performer.getPlayer().sendMessage(ChatColor.YELLOW + "You're too far to unhook that survivor!");
            return;
        }

        performer.startUnhooking(unhookTarget);
    }
}

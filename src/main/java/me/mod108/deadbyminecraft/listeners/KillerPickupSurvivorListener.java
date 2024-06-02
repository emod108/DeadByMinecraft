package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.KillerPickupSurvivorEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KillerPickupSurvivorListener implements Listener {
    @EventHandler
    public void onKillerPickupSurvivor(final KillerPickupSurvivorEvent e) {
        final Killer killer = e.getKiller();

        // Ability check
        if (!killer.canInteractWithSurvivor())
            return;

        // Checking distance
        final Survivor survivor = e.getSurvivor();
        final Location killerLocation = killer.getLocation();
        final Location survivorLocation = survivor.getLocation();
        final double pickupDistance = killerLocation.distanceSquared(survivorLocation);

        if (pickupDistance > Character.ACTION_DISTANCE_SQUARED)
            return;

        killer.pickUp(survivor);
    }
}

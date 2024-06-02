package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.events.HatchInteractEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Hatch;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static me.mod108.deadbyminecraft.targets.characters.Character.ACTION_DISTANCE_SQUARED;

public class HatchInteractListener implements Listener {
    @EventHandler
    public void onHatchInteract(final HatchInteractEvent e) {
        // Checking if the hatch is closed
        final Hatch hatch = e.getHatch();
        if (!hatch.getIsOpen())
            return;

        // Checking, if someone is interacting with the hatch already
        if (hatch.isBeingInteractedWith())
            return;

        // Ability check
        final Character player = e.getPlayer();
        if (!player.canInteractWithHatch())
            return;

        // Distance check
        final Location hatchLocation = hatch.getLocation().clone().add(DeadByMinecraft.CENTERING, 0, DeadByMinecraft.CENTERING);
        if (player.getPlayer().getLocation().distanceSquared(hatchLocation) > ACTION_DISTANCE_SQUARED)
            return;

        // Interacting with the hatch
        player.useHatch(hatch);
    }
}

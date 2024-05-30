package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.ExitGateInteractEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import me.mod108.deadbyminecraft.utility.Directions;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ExitGateInteractListener implements Listener {
    @EventHandler
    public void onExitGateInteract(final ExitGateInteractEvent e) {
        final ExitGate exitGate = e.getExitGate();

        // Checking if gates are openable
        final ExitGate.ExitGateState exitGateState = exitGate.getGateState();
        if (exitGateState == ExitGate.ExitGateState.UNPOWERED || exitGateState == ExitGate.ExitGateState.OPEN)
            return;

        // Checking if someone is interacting with the gates already
        if (exitGate.getInteractingPlayer() != null)
            return;

        final Character player = e.getPlayer();
        // Ability check
        if (!player.canInteractWithExitGate())
            return;

        // Player must be close enough to start opening
        final Location playerLocation = player.getLocation().getBlock().getLocation();
        final Location exitGateLocation = exitGate.getLocation().clone();
        if (!playerLocation.equals(exitGateLocation) && !playerLocation.equals(exitGateLocation.add
                (Directions.getVector(exitGate.getDirection().getOppositeFace(),1))))
            return;

        player.startOpening(exitGate);
    }
}

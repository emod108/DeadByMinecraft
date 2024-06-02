package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.events.LockerInteractEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Locker;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static me.mod108.deadbyminecraft.targets.characters.Character.ACTION_DISTANCE_SQUARED;

public class LockerInteractListener implements Listener {
    @EventHandler
    public void onLockerInteract(final LockerInteractEvent e) {
        final Character player = e.getPlayer();
        final Locker locker = e.getLocker();

        // Ability check
        if (!player.canInteractWithLocker())
            return;

        // If locker is already being interacted with, we can't use it
        if (locker.isBeingInteractedWith())
            return;

        // Getting door location and adding 0.5 so block would be in the center
        final Location lockerDoorLocation = locker.getBottomDoorBlock().getLocation().clone();
        lockerDoorLocation.add(DeadByMinecraft.CENTERING, 0, DeadByMinecraft.CENTERING);

        // Distance check
        if (player.getPlayer().getLocation().distanceSquared(lockerDoorLocation) > ACTION_DISTANCE_SQUARED &&
                player.getMovementState() != Character.MovementState.IN_LOCKER)
            return;

        if (player instanceof final Survivor survivor) {
            // Getting hiding survivor
            final Survivor hidingSurvivor = locker.getHidingSurvivor();

            // Case 1: No one is in the locker
            if (hidingSurvivor == null) {
                // Checking if survivor is already in the locker, so he won't teleport to another locker
                if (survivor.getMovementState() == Character.MovementState.IN_LOCKER)
                    return;

                // Entering the locker
                survivor.enterLocker(locker);
                return;
            }

            // Case 2: The same player is in the locker
            if (survivor.samePlayer(hidingSurvivor)) {
                survivor.leaveLocker(locker);
                return;
            }

            // Case 3: Other survivor is in the locker
            survivor.getPlayer().sendMessage(ChatColor.YELLOW +
                    hidingSurvivor.getPlayer().getDisplayName() + " is hiding in this locker already!");
            return;
        }

        // It's the killer
        ((Killer) player).searchLocker(locker);
    }
}

package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.VaultEvent;
import me.mod108.deadbyminecraft.targets.props.vaultable.Vaultable;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VaultListener implements Listener {
    @EventHandler
    public void onVault(final VaultEvent e) {
        final Vaultable vaultable = e.getVaultable();
        final Character player = e.getPlayer();

        // Getting window location and adding 0.5 so block would be in the center
        final Location windowLocation = vaultable.getLocation().clone();
        windowLocation.add(DeadByMinecraft.CENTERING, 0, DeadByMinecraft.CENTERING);

        // Distance check
        if (player.getPlayer().getLocation().distance(windowLocation) > Character.ACTION_MAX_DISTANCE)
            return;

        // Ability check
        if (!player.canInteractWithWindow())
            return;

        // If window is occupied already
        if (vaultable.isBeingInteractedWith())
            return;

        // Vaulting
        player.vault(vaultable);
    }
}

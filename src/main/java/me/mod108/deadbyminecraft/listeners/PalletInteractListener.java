package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.VaultEvent;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.events.PalletInteractEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PalletInteractListener implements Listener {
    @EventHandler
    public void onPalletInteract(final PalletInteractEvent e) {
        final Pallet pallet = e.getPallet();
        final Character player = e.getPlayer();

        // Getting pallet location and adding 0.5 so block would be in the center
        final Location palletLocation = pallet.getLocation().clone();
        palletLocation.add(DeadByMinecraft.CENTER_ADJUSTMENT, 0, DeadByMinecraft.CENTER_ADJUSTMENT);

        // Distance check
        if (player.getPlayer().getLocation().distance(palletLocation) > Character.ACTION_MAX_DISTANCE)
            return;

        // Ability check
        if (!player.canInteractWithPallet()) {
            return;
        }

        // Can't interact with pallet when they are being broken
        if (pallet.isBeingDestroyed())
            return;

        // Can't interact with pallet when they are being vaulted
        if (pallet.getVaultingPlayer() != null)
            return;

        // If it's the killer
        if (player instanceof final Killer killer) {
            if (pallet.isDropped())
                killer.startBreakingPallet(pallet);
            return;
        }

        // If pallet is not dropped, then survivor can drop it
        if (!pallet.isDropped()) {
            pallet.dropPallet();
            return;
        }

        // If pallet was dropped already, it means we need to vault it
        final VaultEvent vaultEvent = new VaultEvent(player, pallet);
        Bukkit.getServer().getPluginManager().callEvent(vaultEvent);
    }
}

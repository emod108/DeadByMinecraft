package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.VaultEvent;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.events.PalletInteractEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PalletInteractListener implements Listener {
    private static final double PALLET_STUN_DISTANCE = 1.0;

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
            checkPalletStun(pallet);
            return;
        }

        // If pallet was dropped already, it means we need to vault it
        final VaultEvent vaultEvent = new VaultEvent(player, pallet);
        Bukkit.getServer().getPluginManager().callEvent(vaultEvent);
    }

    private void checkPalletStun(final Pallet pallet) {
        // Getting the killer
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;
        final Killer killer = game.getKiller();
        if (killer == null)
            return;

        // If the killer can't be stunned, we don't continue
        if (!killer.canBeStunned())
            return;

        // Getting center of the pallet
        final Location palletLocation = pallet.getLocation();
        final double pX = palletLocation.getX() + 0.5;
        final double pY = palletLocation.getY();
        final double pZ = palletLocation.getZ() + 0.5;

        // Checking if killer is in area
        if (killer.isInArea(pX - PALLET_STUN_DISTANCE, pX + PALLET_STUN_DISTANCE,
                pY - PALLET_STUN_DISTANCE, pY + PALLET_STUN_DISTANCE,
                pZ - PALLET_STUN_DISTANCE, pZ + PALLET_STUN_DISTANCE)) {
            killer.getStunned();
        }
    }
}

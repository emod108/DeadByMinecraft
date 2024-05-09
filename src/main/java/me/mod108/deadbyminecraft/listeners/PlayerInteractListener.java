package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.*;
import me.mod108.deadbyminecraft.targets.props.*;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.targets.props.vaultable.Window;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Wall;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        // Checking if game is going
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        // Checking if it was right click
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        // Checking if player is in the game
        final Character player = game.getPlayer(e.getPlayer());
        if (player == null)
            return;

        // Checking if block is not null
        final Block block = e.getClickedBlock();
        if (block == null)
            return;

        // Interactions with some blocks like doors or trapdoors register only for main hand
        // For other blocks it calls twice for both main and off-hand
        // Therefore, the code must be split between two hands
        final EquipmentSlot hand = e.getHand();
        if (hand == EquipmentSlot.HAND) {
            // Locker interaction
            {
                final Locker locker = findLockerByDoor(block);
                if (locker != null) {
                    // Canceling main event
                    e.setCancelled(true);

                    final LockerInteractEvent event = new LockerInteractEvent(player, locker);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    return;
                }
            }

            // NOT dropped pallet interaction
            {
                final Pallet pallet = findPallet(block);
                if (pallet != null) {
                    // Cancelling main event
                    e.setCancelled(true);

                    final PalletInteractEvent event = new PalletInteractEvent(player, pallet);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    return;
                }
            }

            // Exit gate's switch interaction
            {
                final ExitGate exitGate = findExitGate(block);
                if (exitGate != null) {
                    // Cancelling main event
                    e.setCancelled(true);

                    final ExitGateInteractEvent event = new ExitGateInteractEvent(player, exitGate);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    return;
                }
            }
        }
        else if (hand == EquipmentSlot.OFF_HAND) {
            // Window vaulting
            {
                final Window window = findWindow(block);
                if (window != null) {
                    final VaultEvent event = new VaultEvent(player, window);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    return;
                }
            }

            // Dropped pallet interaction
            {
                final Pallet pallet = findPallet(block);
                if (pallet != null) {
                    final PalletInteractEvent event = new PalletInteractEvent(player, pallet);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    return;
                }
            }

            // Generator interaction
            {
                final Generator generator = findGenerator(block);
                if (generator != null) {
                    final GeneratorInteractEvent event = new GeneratorInteractEvent(player, generator);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    return;
                }
            }

            // Hook interaction
            {
                final Hook hook = findHook(block);
                if (hook != null) {
                    final HookInteractEvent event = new HookInteractEvent(player, hook);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    return;
                }
            }
        }
    }

    // Check if the block is a wall block
    public static boolean isAWallBlock(final Block block) {
        return block.getBlockData() instanceof Wall;
    }

    // Find if the clicked block is a window
    public static Window findWindow(final Block block) {
        // Walls are considered windows
        if (!isAWallBlock(block))
            return null;

        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return null;

        for (final Prop prop : game.getProps()) {
            // The prop must be a window
            if (!(prop instanceof final Window window))
                continue;

            // Checking if clicked block belongs to this locker's door
            if (block.getLocation().equals(window.getLocation()))
                return window;
        }

        // No window found
        return null;
    }

    // Find if the clicked block is a locker
    public static Locker findLockerByDoor(final Block block) {
        // Checking block material
        if (block.getType() != Locker.DOOR_MATERIAL)
            return null;

        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return null;

        for (final Prop prop : game.getProps()) {
            // The prop must be a locker
            if (!(prop instanceof final Locker locker))
                continue;

            // Checking if clicked block belongs to this locker's door
            final Block bottomDoorBlock = locker.getBottomDoorBlock();
            if (bottomDoorBlock.getLocation().equals(block.getLocation()) ||
                    bottomDoorBlock.getLocation().clone().add(0, 1, 0).equals(block.getLocation()))
                return locker;
        }

        // No locker found
        return null;
    }

    // Find if the clicked block is a pallet
    public static Pallet findPallet(final Block block) {
        // Checking block material
        if (block.getType() != Pallet.DROPPED_MATERIAL && block.getType() != Pallet.STANDING_MATERIAL)
            return null;

        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return null;

        for (final Prop prop : game.getProps()) {
            // The prop must be a pallet
            if (!(prop instanceof final Pallet pallet))
                continue;

            // Checking if clicked block belongs to this pallet
            if (block.getLocation().equals(pallet.getPallet().getLocation()))
                return pallet;
        }

        // No pallet found
        return null;
    }

    // Find if the clicked block is a generator
    public static Generator findGenerator(final Block block) {
        // Checking block material
        final Material material = block.getType();
        if (material != Generator.INDOOR_MATERIAL && material != Generator.OUTDOOR_MATERIAL &&
                material != Generator.FRONT_UPPER_MATERIAL && material != Generator.BACK_UPPER_MATERIAL)
            return null;

        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return null;

        for (final Prop prop : game.getProps()) {
            // The prop must be a generator
            if (!(prop instanceof final Generator generator))
                continue;

            // Checking if clicked block belongs to this generator
            for (final Block generatorBlock : generator.getInteractableBlocks())
                if (generatorBlock.getLocation().equals(block.getLocation()))
                    return generator;
        }

        // No generator found
        return null;
    }

    // Find if the clicked block is an exit gate's switch
    public static ExitGate findExitGate(final Block block) {
        // Checking block material
        if (block.getType() != ExitGate.GATE_SWITCH_MATERIAL)
            return null;

        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return null;

        // Checking all props
        for (final Prop prop : game.getProps()) {
            // The prop must be an exit gate
            if (!(prop instanceof final ExitGate exitGate))
                continue;

            if (block.getLocation().equals(exitGate.getGateSwitch().getLocation()))
                return exitGate;
        }

        return null;
    }

    public static Hook findHook(final Block block) {
        // Checking block material
        if (block.getType() != Hook.HOOK_MATERIAL)
            return null;

        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return null;

        // Checking all props
        for (final Prop prop : game.getProps()) {
            // The prop must be a hook
            if (!(prop instanceof final Hook hook))
                continue;

            if (block.getLocation().equals(hook.getHook().getLocation()))
                return hook;
        }

        return null;
    }
}

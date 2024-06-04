package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.*;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
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

import java.util.ArrayList;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        // Checking if game is going
        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return;

        // Checking if player is in the game
        final Character player = game.getPlayer(e.getPlayer());
        if (player == null)
            return;

        // Cancelling the event, because we handle it our own way
        e.setCancelled(true);

        // Getting the block
        final Block block = e.getClickedBlock();

        // Checking if it's a survivor in dying state, so he can start recovering or escape through the hatch
        if (player instanceof final Survivor survivor) {
            if (survivor.getHealthState() == Survivor.HealthState.DYING) {
                if (!survivor.isBeingHealed() && survivor.getAction() == null &&
                        (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) {

                    // Checking if it's a hatch. If it's not, survivor just starts recovering
                    if (block != null) {
                        final Hatch hatch = findHatch(block);
                        if (hatch != null) {
                            final HatchInteractEvent event = new HatchInteractEvent(player, hatch);
                            Bukkit.getServer().getPluginManager().callEvent(event);
                            return;
                        }
                    }
                    survivor.startRecovering();
                }
                return;
            }
        }

        // Checking if it was right click
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        // Checking, if block is not null
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
                    Bukkit.getServer().getPluginManager().callEvent(new LockerInteractEvent(player, locker));
                    return;
                }
            }

            // NOT dropped pallet interaction
            {
                final Pallet pallet = findPallet(block);
                if (pallet != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new PalletInteractEvent(player, pallet));
                    return;
                }
            }

            // Exit gate's switch interaction
            {
                final ExitGate exitGate = findExitGate(block);
                if (exitGate != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new ExitGateInteractEvent(player, exitGate));
                    return;
                }
            }
        }
        else if (hand == EquipmentSlot.OFF_HAND) {
            // Window vaulting
            {
                final Window window = findWindow(block);
                if (window != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new VaultEvent(player, window));
                    return;
                }
            }

            // Dropped pallet interaction
            {
                final Pallet pallet = findPallet(block);
                if (pallet != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new PalletInteractEvent(player, pallet));
                    return;
                }
            }

            // Generator interaction
            {
                final Generator generator = findGenerator(block);
                if (generator != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new GeneratorInteractEvent(player, generator));
                    return;
                }
            }

            // Hook interaction
            {
                final Hook hook = findHook(block);
                if (hook != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new HookInteractEvent(player, hook));
                    return;
                }
            }

            // Hatch trapdoor interaction
            {
                final Hatch hatch = findHatch(block);
                if (hatch != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new HatchInteractEvent(player, hatch));
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

        final ArrayList<Window> windows = game.getWindows();
        for (final Window window : windows) {
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

        final ArrayList<Locker> lockers = game.getLockers();
        for (final Locker locker : lockers) {
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

        final ArrayList<Pallet> pallets = game.getPallets();
        for (final Pallet pallet : pallets) {
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

        final ArrayList<Generator> generators = game.getGenerators();
        for (final Generator generator : generators) {
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

        // Checking all gates
        final ArrayList<ExitGate> exitGates = game.getExitGates();
        for (final ExitGate exitGate : exitGates) {
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

        // Getting hooks
        final ArrayList<Hook> hooks = game.getHooks();
        for (final Hook hook : hooks) {
            if (block.getLocation().equals(hook.getHook().getLocation()))
                return hook;
        }

        return null;
    }

    public static Hatch findHatch(final Block block) {
        // Checking block material
        if (block.getType() != Hatch.HATCH_MATERIAL)
            return null;

        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game == null)
            return null;

        // Getting the hatch
        final Hatch hatch = game.getHatch();
        if (hatch == null)
            return null;

        // Comparing block and hatch locations
        if (block.getLocation().equals(hatch.getLocation()))
            return hatch;

        return null;
    }
}

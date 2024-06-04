package me.mod108.deadbyminecraft.utility;

import me.mod108.deadbyminecraft.targets.props.*;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.targets.props.vaultable.Window;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Wall;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapLoader implements CommandExecutor {
    static private MapData mapData = null;

    public MapData getMapData() {
        return mapData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof final Player player))
            return true;

        // Creates a new map
        if (command.getName().equals("createmap")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "No map name was provided!");
                return true;
            }

            if (mapData != null)
                mapData.hideProps();
            mapData = new MapData(args[0]);
            player.sendMessage(ChatColor.GREEN + "Created new map named \"" + args[0] + "\"");
            return true;
        }

        // Renames currently loaded map
        if (command.getName().equals("renamemap")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "No map name was provided!");
                return true;
            }

            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No maps loaded to rename");
                return true;
            }

            mapData.setMapName(args[0]);
            return true;
        }

        // Loads a map
        if (command.getName().equals("loadmap")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "No map name specified!");
                return true;
            }

            if (mapData != null)
                mapData.hideProps();
            mapData = MapData.loadData(args[0]);
            if (mapData == null)
                player.sendMessage(ChatColor.YELLOW + "Failed to load this map!");
            else
                player.sendMessage(ChatColor.GREEN + "Map was loaded successfully");
            return true;
        }

        // Unloads the map
        if (command.getName().equals("unloadmap")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "Map is already unloaded");
                return true;
            }

            unloadMap();
            player.sendMessage(ChatColor.GREEN + "Map was unloaded successfully");
            return true;
        }

        // Saves the map
        if (command.getName().equals("savemap")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map to save!");
                return true;
            }

            final boolean result = mapData.saveData();
            if (result)
                player.sendMessage(ChatColor.GREEN + "Map saved successfully");
            else
                player.sendMessage(ChatColor.YELLOW + "Failed to save this map!");
            return true;
        }

        // Adds props to currently loaded map
        if (command.getName().equals("addprop")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map loaded to add props to!");
                return true;
            }

            final Prop prop = generateProp(args, player);
            if (prop == null) {
                player.sendMessage(ChatColor.YELLOW + "Failed to add prop to this map!");
                return true;
            }

            mapData.addProp(prop);
            player.sendMessage(ChatColor.GREEN + "Prop added successfully");
            return true;
        }

        // Removes prop on player's location
        if (command.getName().equals("removeprop")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map loaded to remove props from!");
                return true;
            }
            final boolean result = mapData.removePropByLocation(player.getLocation().getBlock().getLocation());
            final String resultStr = result ? ChatColor.GREEN + "Prop removed" : ChatColor.YELLOW + "Prop wasn't found!";
            player.sendMessage(resultStr);
        }

        // Removes last prop
        if (command.getName().equals("removelastprop")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map loaded to remove props from!");
                return true;
            }

            mapData.removeLastProp();
            player.sendMessage(ChatColor.GREEN + "Prop removed successfully");
            return true;
        }

        // Shows props
        if (command.getName().equals("showprops")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map loaded to show props on!");
                return true;
            }

            mapData.showProps();
            player.sendMessage(ChatColor.GREEN + "Props are now showed");
            return true;
        }

        // Hides props
        if (command.getName().equals("hideprops")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map loaded to hide props on!");
                return true;
            }

            mapData.hideProps();
            player.sendMessage(ChatColor.GREEN + "Props are now hidden");
            return true;
        }

        // Adds a spawn point
        if (command.getName().equals("addspawnpoint")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map loaded to add spawn point to!");
                return true;
            }

            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "No spawn point role specified!");
                return true;
            }

            switch (args[0].toUpperCase()) {
                case "KILLER" -> {
                    mapData.addSpawnPoint(player.getLocation(), true);
                    player.sendMessage(ChatColor.YELLOW + "Spawn point added");
                }
                case "SURVIVOR" -> {
                    mapData.addSpawnPoint(player.getLocation(), false);
                    player.sendMessage(ChatColor.YELLOW + "Spawn point added");
                }
                default -> player.sendMessage(ChatColor.YELLOW + "Invalid role");
            }

            return true;
        }

        // Removes a spawn point
        if (command.getName().equals("removespawnpoint")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map loaded to remove spawn point from!");
                return true;
            }

            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "No spawn point role specified!");
                return true;
            }

            switch (args[0].toUpperCase()) {
                case "KILLER" -> {
                    mapData.removeSpawnPoint(true);
                    player.sendMessage(ChatColor.YELLOW + "Spawn point removed");
                }
                case "SURVIVOR" -> {
                    mapData.removeSpawnPoint(false);
                    player.sendMessage(ChatColor.YELLOW + "Spawn point removed");
                }
                default -> player.sendMessage(ChatColor.YELLOW + "Invalid role");
            }
            return true;
        }

        if (command.getName().equals("mapstats")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map is loaded!");
                return true;
            }
            player.sendMessage(mapData.getStats());
        }

        if (command.getName().equals("refreshmap")) {
            if (mapData == null) {
                player.sendMessage(ChatColor.YELLOW + "No map is loaded!");
                return true;
            }
            mapData.refresh();
            player.sendMessage(ChatColor.GREEN + "Map refreshed");
        }

        return true;
    }

    // Helper function, which finds out which prop must be added to the map
    private Prop generateProp(final String[] args, final Player player) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "No prop name provided");
            return null;
        }

        // Getting prop name
        final String propName = args[0];
        Prop prop;
        switch (propName.toUpperCase()) {
            case "LOCKER" -> prop = generateLocker(args, player);
            case "WINDOW" -> prop = generateWindow(args, player);
            case "PALLET" -> prop = generatePallet(args, player);
            case "GENERATOR" -> prop = generateGenerator(args, player);
            case "EXITGATE" -> prop = generateExitGate(args, player);
            case "HOOK" -> prop = generateHook(args, player);
            case "HATCH" -> prop = generateHatch(args, player);
            default -> {
                prop = null;
                player.sendMessage(ChatColor.YELLOW + "Prop with such name wasn't found");
            }
        }

        return prop;
    }

    // Other helper functions, which help in creating props
    private Locker generateLocker(final String[] args, final Player player) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Direction of the locker wasn't specified");
            return null;
        }

        final BlockFace direction = getDirection(args[1], player);
        if (direction == BlockFace.SELF)
            return null;

        return new Locker(player.getLocation(), direction);
    }

    private Window generateWindow(final String[] args, final Player player) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Material of the window wasn't specified");
            return null;
        }

        try {
            final Material material = Material.valueOf(args[1].toUpperCase());
            if (!(material.createBlockData() instanceof Wall))
                player.sendMessage(ChatColor.YELLOW + "Warning! Material provided is not a wall and will be" +
                        "replaced with the default one.");
            return new Window(player.getLocation(), material);
        } catch (final IllegalArgumentException e) {
            player.sendMessage(ChatColor.YELLOW + "Specified windows material wasn't found");
            return null;
        }
    }

    private Pallet generatePallet(final String[] args, final Player player) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Direction of the pallet wasn't specified");
            return null;
        }

        final BlockFace direction = getDirection(args[1], player);
        if (direction == BlockFace.SELF)
            return null;

        return new Pallet(player.getLocation(), direction);
    }

    private Generator generateGenerator(final String[] args, final Player player) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.YELLOW + "To create generator, both direction and type must be provided");
            return null;
        }

        final BlockFace direction = getDirection(args[1], player);
        if (direction == BlockFace.SELF)
            return null;

        final Generator.GeneratorType generatorType;
        switch (args[2].toUpperCase()) {
            case "INDOOR" -> generatorType = Generator.GeneratorType.INDOOR;
            case "OUTDOOR" -> generatorType = Generator.GeneratorType.OUTDOOR;
            default -> {
                player.sendMessage(ChatColor.YELLOW + "Invalid generator type!\nValid types are: INDOOR, OUTDOOR");
                return null;
            }
        }

        return new Generator(player.getLocation(), direction, generatorType);
    }

    private ExitGate generateExitGate(final String[] args, final Player player) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Direction of the exit gate wasn't specified");
            return null;
        }

        final BlockFace direction = getDirection(args[1], player);
        if (direction == BlockFace.SELF)
            return null;

        return new ExitGate(player.getLocation(), direction);
    }

    private Hook generateHook(final String[] args, final Player player) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Direction of the hook wasn't specified");
            return null;
        }

        final BlockFace direction = getDirection(args[1], player);
        if (direction == BlockFace.SELF)
            return null;

        return new Hook(player.getLocation(), direction);
    }

    private BlockFace getDirection(final String directionString, final Player player) {
        final BlockFace direction = switch (directionString.toUpperCase()) {
            case "NORTH" -> BlockFace.NORTH;
            case "SOUTH" -> BlockFace.SOUTH;
            case "EAST" -> BlockFace.EAST;
            case "WEST" -> BlockFace.WEST;
            default -> BlockFace.SELF;
        };

        if (direction == BlockFace.SELF) {
            player.sendMessage(ChatColor.YELLOW + "Invalid direction!\n" +
                    "Valid directions are: North, South, East, West");
        }
        return direction;
    }

    private Hatch generateHatch(final String[] args, final Player player) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Direction of the hatch wasn't specified");
            return null;
        }

        final BlockFace direction = getDirection(args[1], player);
        if (direction == BlockFace.SELF)
            return null;

        return new Hatch(player.getLocation(), direction);
    }

    public void unloadMap() {
        if (mapData != null) {
            mapData.hideProps();
            mapData = null;
        }
    }
}

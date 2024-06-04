package me.mod108.deadbyminecraft.utility;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.props.*;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.targets.props.vaultable.Window;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MapData implements Serializable {
    @Serial
    private static final long serialVersionUID = -2402550899723484126L;

    // How many generators must be on the map
    private final static int REQUIRED_GEN_NUM = 7;

    // How many exit gates must be on the map
    private final static int REQUIRED_GATES_NUM = 2;

    // How many hatches must be on the map
    private final static int REQUIRED_HATCH_NUM = 1;

    // How many hooks must be on the map
    private final static int REQUIRED_HOOKS_NUM = 4;

    // Are props currently visible
    transient private boolean showingProps = false;

    // Map's name
    private String mapName;

    // Props, which will be on this map
    private final ArrayList<Prop> props;

    // How many generators there are on the map
    private int generatorsNum = 0;

    // How many exit gates there are
    private int exitGatesNum = 0;

    // If there's a hatch on this map
    private int hatchNum = 0;

    // How many hooks there are
    private int hooksNum = 0;

    // How many pallets there are
    private int palletNum = 0;

    // How many windows there are
    private int windowsNum = 0;

    // Spawnpoints
    private Location killerSpawn = null;
    private ArrayList<Location> survivorSpawns = new ArrayList<>();

    // Used for saving
    public MapData(final String mapName) {
        this.mapName = mapName;
        props = new ArrayList<>();
    }

    // Used for loading
    public MapData(final MapData loadedData) {
        this.mapName = loadedData.mapName;
        props = loadedData.props;
    }

    public ArrayList<Prop> getProps() {
        return props;
    }

    public void setMapName(final String mapName) {
        this.mapName = mapName;
    }

    public Location getKillerSpawn() {
        return killerSpawn;
    }

    public ArrayList<Location> getSurvivorSpawns() {
        return survivorSpawns;
    }

    // Adds new spawn point. If boolean is false, then this is survivor's spawn point
    public void addSpawnPoint(final Location location, final boolean isKillersSpawn) {
        if (isKillersSpawn)
            killerSpawn = location;
        else
            survivorSpawns.add(location);
    }

    // Removes the last added survivor spawn point. If boolean is true, then sets killer's to null
    public void removeSpawnPoint(final boolean isKillersSpawn) {
        if (isKillersSpawn)
            killerSpawn = null;
        else if (survivorSpawns.size() > 0)
            survivorSpawns.remove(survivorSpawns.size() - 1);
    }

    public void addProp(final Prop prop) {
        props.add(prop);

        // Incrementing one of statistics
        if (prop instanceof Generator)
            ++generatorsNum;
        else if (prop instanceof ExitGate)
            ++exitGatesNum;
        else if (prop instanceof Hatch)
            ++hatchNum;
        else if (prop instanceof Hook)
            ++hooksNum;
        else if (prop instanceof Pallet)
            ++palletNum;
        else if (prop instanceof Window)
            ++windowsNum;

        if (showingProps)
            prop.build();
    }

    public boolean removePropByLocation(final Location location) {
        for (int i = 0; i < props.size(); ++i) {
            final Prop prop = props.get(i);
            if (prop.getLocation().equals(location)) {
                removeProp(i);
                return true;
            }
        }

        return false;
    }

    public void removeLastProp() {
        if (props.size() == 0)
            return;
        removeProp(props.size() - 1);
    }

    private void removeProp(final int index) {
        if (props.size() <= index)
            return;

        final Prop prop = props.get(index);
        props.remove(index);

        // Decrementing one of statistics
        if (prop instanceof Generator)
            --generatorsNum;
        else if (prop instanceof ExitGate)
            --exitGatesNum;
        else if (prop instanceof Hatch)
            --hatchNum;
        else if (prop instanceof Hook)
            --hooksNum;
        else if (prop instanceof Pallet)
            --palletNum;
        else if (prop instanceof Window)
            --windowsNum;

        if (showingProps)
            prop.destroy();
    }

    public void showProps() {
        if (showingProps)
            return;

        showingProps = true;
        for (final Prop prop : props)
            prop.build();
    }

    public void hideProps() {
        if (!showingProps)
            return;

        showingProps = false;
        for (final Prop prop : props)
            prop.destroy();
    }

    // Returns true, if it's a valid map
    public boolean isValid() {
        // Map should have only 7 gens
        if (generatorsNum != REQUIRED_GEN_NUM) {
            System.out.println("Invalid generators num: " + generatorsNum);
            return false;
        }

        // Map should have only 2 exits
        if (exitGatesNum != REQUIRED_GATES_NUM) {
            System.out.println("Invalid exit gates num: " + exitGatesNum);
            return false;
        }

        // Map should have only 1 hatch
        if (hatchNum != REQUIRED_HATCH_NUM) {
            System.out.println("Invalid hatch num: " + hatchNum);
            return false;
        }

        // Map should have at least 4 hooks
        if (hooksNum < REQUIRED_HOOKS_NUM) {
            System.out.println("Hooks number is too low: " + hooksNum);
            return false;
        }

        // Map should have at least 4 survivor spawn locations
        if (survivorSpawns.size() < 4) {
            System.out.println("Number of survivors spawns is too low:" + survivorSpawns.size());
            return false;
        }

        if (killerSpawn == null) {
            System.out.println("No killer spawn");
            return false;
        }
        
        return true;
    }

    public String getStats() {
        final String hasKillerSpawn = killerSpawn == null ? "false" : "true";
        return ChatColor.YELLOW + "Generators: " + generatorsNum + '\n' +
                "Exit gates: " + exitGatesNum + '\n' +
                "Hatches: " + hatchNum + '\n' +
                "Hooks: " + hooksNum + '\n' +
                "Pallets: " + palletNum + '\n' +
                "Windows: " + windowsNum + '\n' +
                "Survivor spawn: " + survivorSpawns.size() + '\n' +
                "Has killer spawn: " + hasKillerSpawn;
    }

    // Refreshes map stats
    public void refresh() {
        generatorsNum = 0;
        exitGatesNum = 0;
        hatchNum = 0;
        hooksNum = 0;

        final ArrayList<Prop> copy = new ArrayList<>(props);
        props.clear();
        for (final Prop prop : copy)
            addProp(prop);
    }

    public boolean saveData() {
        try {
            final String path = DeadByMinecraft.getPlugin().getDataFolder().getPath() + File.separator +
                    DeadByMinecraft.MAPS_FOLDER_NAME + File.separator + mapName;

            final FileOutputStream fileOutputStream = new FileOutputStream(path);
            final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            final BukkitObjectOutputStream out = new BukkitObjectOutputStream(gzipOutputStream);
            out.writeObject(this);
            out.close();
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static MapData loadData(final String mapName) {
        try {
            final String path = DeadByMinecraft.getPlugin().getDataFolder().getPath() + File.separator +
                    DeadByMinecraft.MAPS_FOLDER_NAME + File.separator + mapName;

            final FileInputStream fileInputStream = new FileInputStream(path);
            final GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            final BukkitObjectInputStream in = new BukkitObjectInputStream(gzipInputStream);
            final MapData data = (MapData) in.readObject();
            in.close();
            return data;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

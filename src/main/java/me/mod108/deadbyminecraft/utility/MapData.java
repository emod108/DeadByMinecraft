package me.mod108.deadbyminecraft.utility;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.props.Prop;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MapData implements Serializable {
    // Are props currently visible
    transient private boolean showingProps = false;

    // Map's name
    final String name;

    // Props, which will be on this map
    public final ArrayList<Prop> props;

    // Used for saving
    public MapData(final String name) {
        this.name = name;
        props = new ArrayList<>();
    }

    // Used for loading
    public MapData(final MapData loadedData) {
        this.name = loadedData.name;
        props = loadedData.props;
    }

    public void addProp(final Prop prop) {
        props.add(prop);
        if (showingProps)
            prop.build();
    }

    public void removeLastProp() {
        if (props.size() == 0)
            return;
        final Prop prop = props.get(props.size() - 1);
        props.remove(props.size() - 1);
        if (showingProps)
            prop.destroy();
    }

    public void showProps() {
        if (showingProps)
            return;

        showingProps = true;
        for (final Prop prop : props) {
            prop.build();
        }
    }

    public void hideProps() {
        if (!showingProps)
            return;

        showingProps = false;
        for (final Prop prop : props) {
            prop.destroy();
        }
    }

    public boolean saveData(final String fileName) {
        try {
            final String path = DeadByMinecraft.getPlugin().getDataFolder().getPath() + File.separator +
                    DeadByMinecraft.MAPS_FOLDER_NAME + File.separator + fileName;

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
    public static MapData loadData(final String fileName) {
        try {
            final String path = DeadByMinecraft.getPlugin().getDataFolder().getPath() + File.separator +
                    DeadByMinecraft.MAPS_FOLDER_NAME + File.separator + fileName;

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

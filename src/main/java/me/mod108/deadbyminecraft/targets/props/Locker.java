package me.mod108.deadbyminecraft.targets.props;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.utility.Directions;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("Locker")
public class Locker extends Prop {
    public static final Material LOCKER_MATERIAL = Material.MANGROVE_PLANKS;
    public static final Material DOOR_MATERIAL = Material.MANGROVE_DOOR;
    public static final Sound ENTER_SOUND = Sound.BLOCK_WOODEN_DOOR_OPEN;
    public static final Sound LEAVE_SOUND = Sound.BLOCK_WOODEN_DOOR_CLOSE;

    private Survivor hidingSurvivor = null;
    private Block bottomDoorBlock = null;

    public Locker(final Location location, final BlockFace direction) {
        super(location, direction);
    }

    @Override
    public void build() {
        final Location currentLocation = location.clone();

        // Locker
        placeBlock(currentLocation, LOCKER_MATERIAL);
        placeBlock(currentLocation.add(0, 1, 0), LOCKER_MATERIAL);

        // Locker door
        currentLocation.add(0, -1, 0);
        currentLocation.add(Directions.getVector(direction, 1));
        bottomDoorBlock = placeDoor(currentLocation, DOOR_MATERIAL, direction);
    }

    @Override
    public void destroy() {
        final Survivor survivor = hidingSurvivor;
        if (survivor != null) {
            survivor.teleportFromLocker(this);
            DeadByMinecraft.getPlugin().freezeManager.unFreeze(survivor.getPlayer());
        }

        // Destroying the door manually so it won't drop
        removeBlock(bottomDoorBlock.getLocation(), false);
        removeBlock(bottomDoorBlock.getLocation().clone().add(0, 1, 0), false);
        bottomDoorBlock = null;

        super.destroy();
    }

    public Survivor getHidingSurvivor() {
        return hidingSurvivor;
    }

    public void setHidingSurvivor(final Survivor survivor) {
        hidingSurvivor = survivor;
    }

    public Block getBottomDoorBlock() {
        return bottomDoorBlock;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("location", location);
        map.put("direction", direction);
        return map;
    }

    public static Locker deserialize(Map<String, Object> map) {
        return new Locker((Location) map.get("location"), (BlockFace) map.get("direction"));
    }
}

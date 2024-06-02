package me.mod108.deadbyminecraft.targets.props;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.TrapDoor;

import java.util.HashMap;
import java.util.Map;

public class Hatch extends Prop {
    public static final Material HATCH_MATERIAL = Material.IRON_TRAPDOOR;
    public static final Material HATCH_VOID = Material.BLACK_CONCRETE;

    // Shows, if the hatch is open
    private boolean isOpen = true;

    // Represents material of the block, which was replaced by the hatch
    private Material blockBefore;
    private Location hatchVoid;

    public Hatch(final Location location, final BlockFace direction) {
        super(location, direction);
    }

    @Override
    public void build() {
        final Location currentLocation = location.clone();

        // Placing the hatch
        final Block hatch = placeBlock(currentLocation, HATCH_MATERIAL);
        final TrapDoor trapDoor = (TrapDoor) hatch.getBlockData();
        trapDoor.setFacing(direction);
        trapDoor.setHalf(Bisected.Half.BOTTOM);
        trapDoor.setOpen(true);
        hatch.setBlockData(trapDoor);

        // Remembering which block was replaced by the hatch's void
        blockBefore = currentLocation.add(0, -1, 0).getBlock().getType();
        placeBlock(currentLocation, HATCH_VOID);
        hatchVoid = currentLocation;
    }

    @Override
    public void destroy() {
        super.destroy();
        hatchVoid.getBlock().setType(blockBefore);
    }

    // Returns true if the hatch is open
    public boolean getIsOpen() {
        return isOpen;
    }

    // Closes the hatch
    public void close() {
        isOpen = false;
        final Block hatchBlock = location.getBlock();
        final TrapDoor trapDoor = (TrapDoor) hatchBlock.getBlockData();
        trapDoor.setOpen(false);
        hatchBlock.setBlockData(trapDoor);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("location", location);
        map.put("direction", direction);
        return map;
    }

    public static Hatch deserialize(Map<String, Object> map) {
        return new Hatch((Location) map.get("location"), (BlockFace) map.get("direction"));
    }
}

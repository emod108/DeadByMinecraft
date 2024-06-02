package me.mod108.deadbyminecraft.targets.props;

import me.mod108.deadbyminecraft.targets.Target;
import me.mod108.deadbyminecraft.targets.characters.Character;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.Map;

public abstract class Prop implements Target, ConfigurationSerializable {
    // Location of the prop. All blocks are being build relatively from this location
    protected final Location location;

    // Which direction the prop is facing
    protected final BlockFace direction;

    // Blocks of the prop
    protected final ArrayList<Block> blocks = new ArrayList<>();

    // Player, who currently interacts with this prop
    protected Character interactingPlayer = null;

    public Prop(final Location location, final BlockFace direction) {
        // Making deep copy
        this.location = new Location(location.getWorld(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
        this.direction = direction;
    }

    public boolean isBeingInteractedWith() {
        return interactingPlayer != null;
    }

    public Character getInteractingPlayer() {
        return interactingPlayer;
    }

    public void setInteractingPlayer(final Character player) {
        interactingPlayer = player;
    }

    // Method, which defines how the prop looks like
    public abstract void build();

    // Destroys the prop
    public void destroy() {
        if (interactingPlayer != null)
            interactingPlayer.cancelAction();

        for (final Block block : blocks)
            block.setType(Material.AIR);
        blocks.clear();
    }

    // Places a block and returns a reference to it
    // Placing a block to the same location will add it twice!
    protected Block placeBlock(final Location location, final Material material, final boolean physics) {
        final Block block = location.getBlock();
        block.setType(material, physics);
        blocks.add(block);

        return block;
    }

    // Same method, but which doesn't accept physics argument
    protected Block placeBlock(final Location location, final Material material) {
        final Block block = location.getBlock();
        block.setType(material);
        blocks.add(block);

        return block;
    }

    // Removes block from the list and replaces it with air
    protected void removeBlock(final Location location, final boolean physics) {
        for (int i = 0; i < blocks.size(); ++i) {
            if (location.equals(blocks.get(i).getLocation())) {
                blocks.get(i).setType(Material.AIR, physics);
                blocks.remove(i);
                return;
            }
        }
    }

    protected Block placeDoor(final Location location, final Material material, final BlockFace direction) {
        final Location currentLocation = location.clone();
        final Block top = placeBlock(currentLocation.add(0, 1, 0), material, false);
        final Block bottom = placeBlock(currentLocation.add(0, -1, 0), material, false);

        final Door topDoor = (Door) top.getBlockData();
        final Door bottomDoor = (Door) bottom.getBlockData();

        topDoor.setHalf(Bisected.Half.TOP);
        bottomDoor.setHalf(Bisected.Half.BOTTOM);

        topDoor.setFacing(direction);
        bottomDoor.setFacing(direction);

        top.setBlockData(topDoor);
        bottom.setBlockData(bottomDoor);

        return bottom;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    // Returns true if there are blocks in the arraylist
    public boolean isBuilt() {
        return blocks.size() > 0;
    }

    @Override
    abstract public Map<String, Object> serialize();
}

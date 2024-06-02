package me.mod108.deadbyminecraft.targets.props.vaultable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Wall;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("Window")
public class Window extends Vaultable {
    private static final Sound VAULT_SOUND = Sound.BLOCK_FENCE_GATE_CLOSE;
    private static final Material DEFAULT_MATERIAL = Material.STONE_BRICK_WALL;

    private final Material wallMaterial;

    public Window(final Location location, final Material wallMaterial) {
        super(location, BlockFace.NORTH); // Direction doesn't matter for this block

        // Material provided must be a wall
        this.wallMaterial = (wallMaterial.createBlockData() instanceof Wall) ? wallMaterial : DEFAULT_MATERIAL;
    }

    @Override
    public void build() {
        placeBlock(getLocation(), wallMaterial);
    }

    // Returns true if you can vault only East and West
    // Returns false if you can vault only North and South
    @Override
    public boolean canVaultEastAndWest() {
        // Finding vaulting direction
        final Wall wall = (Wall) location.getBlock().getBlockData();
        final Wall.Height heightNorth = wall.getHeight(BlockFace.NORTH);

        // If heightNorth = LOW, then we are vaulting EAST or WEST
        // if heightNorth = NONE, then we are vaulting NORTH or SOUTH
        return (heightNorth == Wall.Height.LOW);
    }

    @Override
    public Sound getVaultingSound() {
        return VAULT_SOUND;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("location", location);
        map.put("material", wallMaterial);
        return map;
    }

    public static Window deserialize(Map<String, Object> map) {
        return new Window((Location) map.get("location"), (Material) map.get("material"));
    }
}

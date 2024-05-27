package me.mod108.deadbyminecraft.targets.props.vaultable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Wall;

public class Window extends Vaultable {
    private static final Sound VAULT_SOUND = Sound.BLOCK_FENCE_GATE_CLOSE;

    private final Material wallMaterial;

    public Window(final Location location, final Material wallMaterial) {
        super(location, BlockFace.NORTH); // Direction doesn't matter for this block
        this.wallMaterial = wallMaterial;
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
}

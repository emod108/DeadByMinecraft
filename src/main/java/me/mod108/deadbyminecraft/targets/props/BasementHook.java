package me.mod108.deadbyminecraft.targets.props;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public abstract class BasementHook extends Hook {
    public static final Material STAND_MATERIAL = Material.NETHER_BRICK_FENCE;

    public BasementHook(final Location location) {
        super(location, BlockFace.NORTH);
    }
}

package me.mod108.deadbyminecraft.targets.props;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public abstract class Hatch extends Prop {
    public Hatch(Location location, BlockFace direction) {
        super(location, direction);
    }
}

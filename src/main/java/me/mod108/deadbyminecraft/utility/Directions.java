package me.mod108.deadbyminecraft.utility;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class Directions {
    public static Vector getVector(final BlockFace direction, final int vectorValue) {
        final Vector vector = new Vector();

        return switch (direction) {
            case NORTH -> vector.setZ(-vectorValue);
            case SOUTH -> vector.setZ(vectorValue);
            case EAST -> vector.setX(vectorValue);
            case WEST -> vector.setX(-vectorValue);
            default -> vector;
        };
    }

    public static BlockFace turnLeft(final BlockFace direction) {
        return switch (direction) {
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            case WEST -> BlockFace.SOUTH;
            case EAST -> BlockFace.NORTH;
            default -> BlockFace.SELF;
        };
    }

    public static BlockFace turnRight(final BlockFace direction) {
        return switch (direction) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            case EAST -> BlockFace.SOUTH;
            default -> BlockFace.SELF;
        };
    }
}

package me.mod108.deadbyminecraft.utility;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

// This class represents escape line, which survivors must cross to escape
public class EscapeLine {
    // How tall is the wall which blocks killers from exiting
    private static final int BLOCKER_HEIGHT = ExitGate.GATES_HEIGHT;

    private static final int BLOCKER_WIDTH = ExitGate.GATES_WIDTH;

    // From how far blocking wall is visible to the killer
    private static final double BLOCKER_VISIBLE_DISTANCE = 4;
    public static final double BLOCKER_VISIBLE_DISTANCE_SQUARED = BLOCKER_VISIBLE_DISTANCE * BLOCKER_VISIBLE_DISTANCE;

    // Escape point
    private final Location escapePoint;

    // Which way this line should be crossed
    private final BlockFace direction;

    // Task, which show blockers to the killer
    private BukkitRunnable blockerTask = null;

    // Material, which makes up blockers and doesn't allow the killer to cross it
    private BlockData blocker;

    private boolean visibleToKiller = false;

    public EscapeLine(final Location escapePoint, final BlockFace direction) {
        this.escapePoint = escapePoint;
        this.direction = direction;

        final String directionsString = direction.getModX() == 0 ?
                "north=false,south=false,east=true,west=true]" :
                "north=true,south=true,east=false,west=false]";
        final String dataString = "minecraft:iron_bars[waterlogged=false," + directionsString;
        try {
            blocker = Bukkit.createBlockData(dataString);
        } catch (final IllegalArgumentException e) {
            System.out.println("Exception while creating gate blockers!");
            blocker = Bukkit.createBlockData(Material.IRON_BARS);
        }
    }

    // Returns true, if survivor has crossed the line
    // Works by comparing survivor coordinates to one of the line's points
    public boolean hasCrossedIt(final Survivor survivor) {
        final Location location = survivor.getLocation();
        final Location pointLocation = escapePoint.clone().add(DeadByMinecraft.CENTERING, 0, DeadByMinecraft.CENTERING);

        return switch (direction) {
            case NORTH -> location.getZ() < pointLocation.getZ();
            case EAST -> location.getX() > pointLocation.getX();
            case SOUTH -> location.getZ() > pointLocation.getZ();
            case WEST -> location.getX() < pointLocation.getX();
            default -> false;
        };
    }

    // Gets 1d distance from blocking wall to the killer
    public double getDistanceSquaredToKiller(final Killer killer) {
        final Location killerLocation = killer.getLocation().clone();
        final Location pointLocation = escapePoint.clone().add(DeadByMinecraft.CENTERING, 0, DeadByMinecraft.CENTERING);

        killerLocation.setY(0);
        pointLocation.setY(0);

        // NORTH or SOUTH
        if (direction.getModX() == 0) {
            killerLocation.setX(0);
            pointLocation.setX(0);
        } else { // EAST or WEST
            killerLocation.setZ(0);
            pointLocation.setZ(0);
        }

        return killerLocation.distanceSquared(pointLocation);
    }

    // Shows blocking wall to the killer
    public void showToKiller(final Killer killer) {
        visibleToKiller = true;
        if (blockerTask == null) {
            blockerTask = new BukkitRunnable() {
                final Player player = killer.getPlayer();
                final Vector blockerVector = Directions.getVector(Directions.turnRight(direction), 1);

                @Override
                public void run() {
                    // Showing blockers
                    final Location currentLocation = escapePoint.clone();
                    for (int i = 0; i < BLOCKER_WIDTH; ++i) {
                        for (int j = 0; j < BLOCKER_HEIGHT; ++j) {
                            player.sendBlockChange(currentLocation, blocker);
                            currentLocation.add(0, 1, 0);
                        }
                        currentLocation.add(blockerVector);
                        currentLocation.add(0, -BLOCKER_HEIGHT, 0);
                    }
                }
            };
            blockerTask.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
        }
    }

    // Hides blocking wall to the killer
    public void hideFromKiller(final Killer killer) {
        // Stopping the task
        visibleToKiller = false;
        if (blockerTask != null) {
            blockerTask.cancel();
            blockerTask = null;
        }

        final Player player = killer.getPlayer();
        final Vector blockerVector = Directions.getVector(Directions.turnRight(direction), 1);

        // Hiding all blocking blocks
        final Location currentLocation = escapePoint.clone();
        for (int i = 0; i < BLOCKER_WIDTH; ++i) {
            for (int j = 0; j < BLOCKER_HEIGHT; ++j) {
                player.sendBlockChange(currentLocation, currentLocation.getBlock().getBlockData());
                currentLocation.add(0, 1, 0);
            }
            currentLocation.add(blockerVector);
            currentLocation.add(0, -BLOCKER_HEIGHT, 0);
        }
    }

    // Shows if blocking wall is visible to the killer
    public boolean isVisibleToKiller() {
        return visibleToKiller;
    }
}

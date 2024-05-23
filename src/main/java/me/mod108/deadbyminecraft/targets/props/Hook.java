package me.mod108.deadbyminecraft.targets.props;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.utility.Directions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Hook extends Prop {
    public static final Material STAND_MATERIAL = Material.OAK_FENCE;
    public static final Material HOOK_MATERIAL = Material.IRON_BARS;
    private static final int STAND_HEIGHT = 5;

    private Survivor hookedSurvivor = null;
    private Block hook = null;

    public Hook(final Location location, final BlockFace direction) {
        super(location, direction);
    }

    @Override
    public void build() {
        final Location currentLocation = location.clone();

        for (int i = 0; i < STAND_HEIGHT; ++i) {
            placeBlock(currentLocation, STAND_MATERIAL);
            currentLocation.add(0, 1, 0);
        }
        currentLocation.add(0, -1, 0);

        currentLocation.add(Directions.getVector(direction, 1));
        placeBlock(currentLocation, STAND_MATERIAL);

        currentLocation.add(0, -1, 0);
        hook = placeBlock(currentLocation, HOOK_MATERIAL);
    }

    @Override
    public void destroy() {
        if (hookedSurvivor != null)
            hookedSurvivor.getUnhooked(this);

        super.destroy();
    }

    public Survivor getHookedSurvivor() {
        return hookedSurvivor;
    }

    public void hook(final Survivor survivor) {
        if (hookedSurvivor != null)
            return;
        if (isBroken())
            return;
        hookedSurvivor = survivor;

        // Spawning a barrier block
        placeBlock(hook.getRelative(0, -3, 0).getLocation(), Material.BARRIER);
    }

    public void unHook() {
        if (hookedSurvivor == null)
            return;
        if (isBroken())
            return;

        hookedSurvivor = null;
        // Removing the barrier block
        removeBlock(hook.getRelative(0, -3, 0).getLocation(), true);
    }

    public Block getHook() {
        return hook;
    }

    // Returns true if the hook is broken
    public boolean isBroken() {
        return hook == null;
    }

    // Returns true if survivor can be hooked here
    public boolean availableForHooking() {
        return !isBroken() && hookedSurvivor == null;
    }

    // Makes the hook broken
    public void becomeBroken() {
        if (hook == null)
            return;

        removeBlock(hook.getRelative(0, -3, 0).getLocation(), true);
        removeBlock(hook.getLocation(), true);
        hookedSurvivor = null;
        hook = null;
    }
}

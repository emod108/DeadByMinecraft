package me.mod108.deadbyminecraft.targets.props.vaultable;

import me.mod108.deadbyminecraft.managers.SoundManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.TrapDoor;

public class Pallet extends Vaultable {
    public static final Material STANDING_MATERIAL = Material.SPRUCE_TRAPDOOR;
    public static final Material DROPPED_MATERIAL = Material.SPRUCE_SLAB;
    public static final Sound DROP_SOUND = Sound.ITEM_SHIELD_BLOCK;
    public static final Sound VAULT_SOUND = Sound.BLOCK_CHEST_OPEN;
    public static final Sound BREAK_SOUND = Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;

    // Shows if pallet is dropped
    private boolean dropped = false;

    // Pallet. Its location is different from prop's location
    private Block pallet;

    // True when pallet is being destroyed by killer
    private boolean beingDestroyed = false;

    public Pallet(final Location location, final BlockFace direction) {
        super(location, direction);
    }

    @Override
    public void build() {
        // Pallet spawns one block above its location
        final Location palletLocation = location.clone().add(0, 1, 0);

        // Spawning pallet
        pallet = placeBlock(palletLocation, STANDING_MATERIAL);
        final TrapDoor trapDoor = (TrapDoor) pallet.getBlockData();
        trapDoor.setFacing(direction);
        trapDoor.setOpen(true);
        pallet.setBlockData(trapDoor);
    }

    @Override
    public boolean canVaultEastAndWest() {
        return (direction == BlockFace.NORTH || direction == BlockFace.SOUTH);
    }

    public boolean isDropped() {
        return dropped;
    }

    public void dropPallet() {
        dropped = true;
        pallet.setType(DROPPED_MATERIAL);
        placeBlock(location, Material.BARRIER);
        SoundManager.playForAll(location, DROP_SOUND, 1.0f, 1.0f);
    }

    public Block getPallet() {
        return pallet;
    }

    public boolean isBeingDestroyed() {
        return beingDestroyed;
    }

    public void startDestroying() {
        beingDestroyed = true;
    }
}

package me.mod108.deadbyminecraft.targets.props.vaultable;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.props.Breakable;
import me.mod108.deadbyminecraft.targets.props.Hook;
import me.mod108.deadbyminecraft.utility.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("Pallet")
public class Pallet extends Vaultable implements Breakable {
    public static final Material STANDING_MATERIAL = Material.SPRUCE_TRAPDOOR;
    public static final Material DROPPED_MATERIAL = Material.SPRUCE_SLAB;
    public static final Sound DROP_SOUND = Sound.ITEM_SHIELD_BLOCK;
    private static final Sound VAULT_SOUND = Sound.BLOCK_CHEST_OPEN;

    private static final Sound BREAK_SOUND = Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;
    private static final float BREAK_TIME = 2.3f;

    // Shows if pallet is dropped
    private boolean dropped = false;

    // Pallet. Its location is different from prop's location
    private Block pallet;

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

    @Override
    public Sound getVaultingSound() {
        return VAULT_SOUND;
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

    @Override
    public Sound getBreakingSound() {
        return BREAK_SOUND;
    }

    @Override
    public float getBreakingTime() {
        return BREAK_TIME;
    }

    @Override
    public void getBroken() {
        destroy();

        final Game game = DeadByMinecraft.getPlugin().getGame();
        if (game != null)
            game.removeProp(this);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("location", location);
        map.put("direction", direction);
        return map;
    }

    public static Pallet deserialize(Map<String, Object> map) {
        return new Pallet((Location) map.get("location"), (BlockFace) map.get("direction"));
    }
}

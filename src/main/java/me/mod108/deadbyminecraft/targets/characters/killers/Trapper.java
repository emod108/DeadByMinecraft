package me.mod108.deadbyminecraft.targets.characters.killers;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Trapper extends Killer {
    public Trapper(final Player player) {
        super(player, DEFAULT_SPEED);
    }

    @Override
    public void applyKillerKit() {
        final PlayerInventory inventory = player.getInventory();

        // Creating helmet
        final ItemStack helmet = createKillerItem(Material.SKELETON_SKULL,
                "Trapper's Mask");
        inventory.setHelmet(helmet);

        // Creating chestplate
        final ItemStack chestplate = createKillerItem(Material.LEATHER_CHESTPLATE,
                "Trapper's Apron");
        inventory.setChestplate(chestplate);

        // Creating leggings
        final ItemStack leggings = createKillerItem(Material.LEATHER_LEGGINGS,
                "Trapper's Pants");
        inventory.setLeggings(leggings);

        // Creating boots
        final ItemStack boots = createKillerItem(Material.LEATHER_BOOTS,
                "Trapper's Boots");
        inventory.setBoots(boots);

        // Creating base weapon
        final ItemStack baseWeapon = createKillerItem(Material.IRON_SWORD,
                "Trapper's Cleaver");
        inventory.addItem(baseWeapon);

        // Creating traps
    }

    @Override
    public String getKillerName() {
        return KILLER_NAMES[0];
    }

    @Override
    public Sound getStunSounds() {
        return Sound.ENTITY_CAMEL_HURT;
    }
}

package me.mod108.deadbyminecraft.items;

import me.mod108.deadbyminecraft.targets.Target;
import me.mod108.deadbyminecraft.targets.characters.Character;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Item {
    private final ItemStack item;

    public Item(final String itemName, final Material itemType) {
        item = new ItemStack(itemType);

        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(itemName);
            item.setItemMeta(meta);
        }
    }

    public ItemStack getItem() {
        return item;
    }

    public abstract void use(final Character performer, final Target target);
}

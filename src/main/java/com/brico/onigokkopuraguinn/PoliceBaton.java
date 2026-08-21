package com.brico.onigokkopuraguinn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 警察用の警棒（名前付き木の棒）。
 */
public final class PoliceBaton {

    public static final String DISPLAY_NAME = "警棒";

    private static NamespacedKey key;

    private PoliceBaton() {}

    public static void init(JavaPlugin plugin) {
        key = new NamespacedKey(plugin, "police_baton");
    }

    public static ItemStack create() {
        ItemStack stick = new ItemStack(Material.STICK);
        stick.editMeta(meta -> {
            meta.displayName(Component.text(DISPLAY_NAME, NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            if (key != null) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            }
        });
        return stick;
    }

    public static boolean isBaton(ItemStack item) {
        if (item == null || item.getType() != Material.STICK || key == null) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}

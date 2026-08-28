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
 * 泥棒用の凍結雪玉。
 */
public final class ThiefSnowball {

    public static final String DISPLAY_NAME = "凍結の雪玉";
    public static final int AMOUNT = 5;

    private static NamespacedKey key;

    private ThiefSnowball() {}

    public static void init(JavaPlugin plugin) {
        key = new NamespacedKey(plugin, "thief_snowball");
    }

    public static NamespacedKey key() {
        return key;
    }

    public static ItemStack create(int amount) {
        ItemStack snowball = new ItemStack(Material.SNOWBALL, amount);
        snowball.editMeta(meta -> {
            meta.displayName(Component.text(DISPLAY_NAME, NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(java.util.List.of(
                    Component.text("警察に当てると3秒間動けなくする", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("クールダウン: 30秒", NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            if (key != null) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            }
        });
        return snowball;
    }

    public static boolean isThiefSnowball(ItemStack item) {
        if (item == null || item.getType() != Material.SNOWBALL || key == null) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}

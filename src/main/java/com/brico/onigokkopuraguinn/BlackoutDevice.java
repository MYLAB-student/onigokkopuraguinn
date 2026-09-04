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
 * 停電装置（ブレイズパウダー）。警察を30秒盲目＋走行不可にする。
 */
public final class BlackoutDevice {

    public static final String DISPLAY_NAME = "停電装置";

    private static NamespacedKey key;

    private BlackoutDevice() {}

    public static void init(JavaPlugin plugin) {
        key = new NamespacedKey(plugin, "blackout_device");
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        item.editMeta(meta -> {
            meta.displayName(Component.text(DISPLAY_NAME, NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(java.util.List.of(
                    Component.text("使うと警察の視界を30秒奪う", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("使用後はチェストに戻る（使い切り）", NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            if (key != null) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            }
        });
        return item;
    }

    public static boolean isBlackoutDevice(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_POWDER || key == null) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}

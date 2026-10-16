package com.brico.onigokkopuraguinn.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * チェスト・作業台・書見台以外のコンテナ／GUIブロックを開けないようにする。
 */
public class ContainerRestrictListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();
        if (type == Material.CHEST
                || type == Material.TRAPPED_CHEST
                || type == Material.CRAFTING_TABLE
                || type == Material.LECTERN) {
            return;
        }

        if (!isRestrictedInteractable(type)) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        InventoryType type = event.getInventory().getType();
        if (type == InventoryType.CHEST
                || type == InventoryType.WORKBENCH
                || type == InventoryType.CRAFTING
                || type == InventoryType.CREATIVE
                || type == InventoryType.PLAYER
                || type == InventoryType.LECTERN) {
            return;
        }

        event.setCancelled(true);
    }

    private static boolean isRestrictedInteractable(Material type) {
        return switch (type) {
            case BARREL,
                 ENDER_CHEST,
                 HOPPER,
                 DROPPER,
                 DISPENSER,
                 FURNACE,
                 BLAST_FURNACE,
                 SMOKER,
                 BREWING_STAND,
                 ANVIL,
                 CHIPPED_ANVIL,
                 DAMAGED_ANVIL,
                 ENCHANTING_TABLE,
                 GRINDSTONE,
                 LOOM,
                 CARTOGRAPHY_TABLE,
                 STONECUTTER,
                 SMITHING_TABLE,
                 BEACON -> true;
            default -> type.name().endsWith("SHULKER_BOX");
        };
    }
}

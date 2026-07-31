package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.ChestHighlightManager;
import com.brico.onigokkopuraguinn.GameManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class HighlightProtectListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (isHighlight(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (target != null && isHighlight(target)) {
            event.setCancelled(true);
        }
        if (isHighlight(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!isHighlight(clicked)) return;

        event.setCancelled(true);
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Block block = clicked.getLocation().getBlock();

        // ブレイズロッド所持時は選択、それ以外はチェストを開く
        if (player.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
            selectChest(player, block);
            return;
        }

        openChest(player, block);
    }

    private static void selectChest(Player player, Block block) {
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
            return;
        }

        GameManager manager = GameManager.getInstance();
        boolean added = manager.addChest(block.getLocation());
        if (!added) {
            player.sendMessage("§e[ゲーム] このチェストはすでに選択されています。"
                    + " (選択数: " + manager.getChestLocations().size() + ")");
            return;
        }

        int count = manager.getChestLocations().size();
        int required = GameManager.requiredChestCount();
        String status = count >= required
                ? " §a(必要数 " + required + " 達成！ /gamestart で開始)"
                : " §7(あと " + (required - count) + " 個)";
        player.sendMessage("§a[ゲーム] チェストを選択しました。(選択数: " + count + ")" + status);
    }

    private static void openChest(Player player, Block block) {
        if (!(block.getState() instanceof Chest chest)) {
            return;
        }
        player.openInventory(chest.getInventory());
    }

    private static boolean isHighlight(Entity entity) {
        ChestHighlightManager manager = ChestHighlightManager.getInstance();
        return manager != null && manager.isHighlightEntity(entity);
    }
}

package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ChestSetListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.BLAZE_ROD) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) return;

        event.setCancelled(true);

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
}

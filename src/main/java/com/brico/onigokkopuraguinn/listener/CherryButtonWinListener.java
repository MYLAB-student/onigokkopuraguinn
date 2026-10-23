package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameEndHelper;
import com.brico.onigokkopuraguinn.GameManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * 桜のボタンを押すと泥棒の勝ちでゲーム終了。
 */
public class CherryButtonWinListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHERRY_BUTTON) return;

        if (GameManager.getInstance().getRoles().isEmpty()) return;

        Player presser = event.getPlayer();
        List<Player> players = new ArrayList<>(presser.getWorld().getPlayers());
        GameEndHelper.thievesWin(players);

        for (Player player : players) {
            player.sendMessage("§c[ゲーム] " + presser.getName() + " が桜のボタンを押したため、泥棒の勝ちです！");
        }
    }
}

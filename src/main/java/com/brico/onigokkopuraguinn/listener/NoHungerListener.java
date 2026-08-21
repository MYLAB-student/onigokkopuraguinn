package com.brico.onigokkopuraguinn.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * プレイヤーがおなかを空かさないようにする。
 */
public class NoHungerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        fillHunger(event.getPlayer());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        event.setCancelled(true);
        fillHunger(player);
    }

    public static void fillHunger(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);
    }
}

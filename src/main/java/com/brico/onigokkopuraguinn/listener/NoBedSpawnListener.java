package com.brico.onigokkopuraguinn.listener;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * ベッドでリスポーン地点を固定できないようにする。
 */
public class NoBedSpawnListener implements Listener {

    @EventHandler
    public void onSetSpawn(PlayerSetSpawnEvent event) {
        if (event.getCause() != PlayerSetSpawnEvent.Cause.BED) return;
        event.setCancelled(true);
    }
}

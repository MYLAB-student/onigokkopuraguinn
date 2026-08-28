package com.brico.onigokkopuraguinn.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 畑（農地）を踏み荒らせないようにする。
 */
public class FarmlandProtectListener implements Listener {

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPhysicalInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }
}

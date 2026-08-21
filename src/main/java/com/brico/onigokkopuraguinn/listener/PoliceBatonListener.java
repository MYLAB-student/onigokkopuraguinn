package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.PoliceBaton;
import com.brico.onigokkopuraguinn.Role;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * 警察が警棒で泥棒を殴ると、牢屋座標へテレポートさせる。
 */
public class PoliceBatonListener implements Listener {

    private static final double JAIL_X = 36;
    private static final double JAIL_Y = -60;
    private static final double JAIL_Z = -82;

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        GameManager manager = GameManager.getInstance();
        if (manager.getRole(attacker) != Role.POLICE) return;
        if (manager.getRole(victim) != Role.THIEF) return;
        if (!PoliceBaton.isBaton(attacker.getInventory().getItemInMainHand())) return;

        event.setCancelled(true);

        Location jail = new Location(victim.getWorld(), JAIL_X + 0.5, JAIL_Y, JAIL_Z + 0.5);
        victim.teleport(jail);

        attacker.sendMessage("§9[ゲーム] " + victim.getName() + " を捕まえました！");
        victim.sendMessage("§c[ゲーム] 警察に捕まりました！");
    }
}

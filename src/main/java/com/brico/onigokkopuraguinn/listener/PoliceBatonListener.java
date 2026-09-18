package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.PoliceBaton;
import com.brico.onigokkopuraguinn.Role;
import com.brico.onigokkopuraguinn.ThiefCatchState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * 警察の警棒：1回目はスタート地点、2回目は牢屋。脱獄でカウントリセット。殴られた後5秒無敵。
 */
public class PoliceBatonListener implements Listener {

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        GameManager manager = GameManager.getInstance();
        if (manager.getRole(attacker) != Role.POLICE) return;
        if (manager.getRole(victim) != Role.THIEF) return;
        if (!PoliceBaton.isBaton(attacker.getInventory().getItemInMainHand())) return;

        event.setCancelled(true);

        if (ThiefCatchState.isBatonImmune(victim)) {
            attacker.sendMessage("§e[ゲーム] " + victim.getName() + " はあと "
                    + ThiefCatchState.remainingImmunitySeconds(victim) + " 秒無敵です。");
            return;
        }

        int hits = ThiefCatchState.incrementHit(victim);
        ThiefCatchState.grantBatonImmunity(victim);

        if (hits == 1) {
            victim.teleport(ThiefCatchState.thiefSpawn(victim.getWorld()));
            attacker.sendMessage("§9[ゲーム] " + victim.getName() + " をスタート地点へ追い返しました！（1回目）");
            victim.sendMessage("§c[ゲーム] 警察に捕まり、スタート地点へ戻されました！（あと1回で牢屋）");
            return;
        }

        // 2回目以降は牢屋
        ThiefCatchState.markInJail(victim);
        victim.teleport(ThiefCatchState.jail(victim.getWorld()));
        attacker.sendMessage("§9[ゲーム] " + victim.getName() + " を牢屋へ送りました！");
        victim.sendMessage("§c[ゲーム] 警察に捕まり、牢屋へ送られました！脱出するとリセットされます。");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        // ブロックをまたいだときだけ判定
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (GameManager.getInstance().getRole(player) != Role.THIEF) return;
        if (!ThiefCatchState.isInJail(player)) return;

        if (ThiefCatchState.tryEscapeJail(player)) {
            player.sendMessage("§a[ゲーム] 牢屋から脱出しました！被弾回数がリセットされました。");
        }
    }
}

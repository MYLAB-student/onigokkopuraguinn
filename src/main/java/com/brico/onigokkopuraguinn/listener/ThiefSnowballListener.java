package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.PlayerFreeze;
import com.brico.onigokkopuraguinn.Role;
import com.brico.onigokkopuraguinn.ThiefSnowball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 泥棒の凍結雪玉：警察に当てると3秒拘束、クールダウン30秒。
 */
public class ThiefSnowballListener implements Listener {

    private static final long COOLDOWN_MILLIS = 30_000L;
    private static final long FREEZE_TICKS = 3L * 20L;

    private final Map<UUID, Long> cooldownUntil = new HashMap<>();

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (!(snowball.getShooter() instanceof Player shooter)) return;
        if (GameManager.getInstance().getRole(shooter) != Role.THIEF) return;

        ItemStack hand = shooter.getInventory().getItemInMainHand();
        if (!ThiefSnowball.isThiefSnowball(hand)) {
            hand = shooter.getInventory().getItemInOffHand();
            if (!ThiefSnowball.isThiefSnowball(hand)) return;
        }

        if (isOnCooldown(shooter)) {
            event.setCancelled(true);
            shooter.sendMessage("§c[ゲーム] 凍結雪玉はクールダウン中です。（残り "
                    + remainingCooldownSeconds(shooter) + " 秒）");
            return;
        }

        if (ThiefSnowball.key() != null) {
            snowball.getPersistentDataContainer().set(ThiefSnowball.key(), PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (ThiefSnowball.key() == null) return;
        if (!snowball.getPersistentDataContainer().has(ThiefSnowball.key(), PersistentDataType.BYTE)) return;
        if (!(snowball.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player hit)) return;

        GameManager manager = GameManager.getInstance();
        if (manager.getRole(shooter) != Role.THIEF) return;
        if (manager.getRole(hit) != Role.POLICE) return;
        if (isOnCooldown(shooter)) return;

        cooldownUntil.put(shooter.getUniqueId(), System.currentTimeMillis() + COOLDOWN_MILLIS);

        PlayerFreeze freeze = PlayerFreeze.getInstance();
        if (freeze != null) {
            freeze.freeze(hit, FREEZE_TICKS, "§a[ゲーム] 凍結が解けました。");
        }

        shooter.sendMessage("§b[ゲーム] 警察を3秒間凍らせました！（クールダウン 30秒）");
        hit.sendMessage("§c[ゲーム] 凍結雪玉を受けて3秒間動けません！");
    }

    private boolean isOnCooldown(Player player) {
        Long until = cooldownUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    private long remainingCooldownSeconds(Player player) {
        Long until = cooldownUntil.get(player.getUniqueId());
        if (until == null) return 0;
        return Math.max(0, (until - System.currentTimeMillis() + 999) / 1000);
    }
}

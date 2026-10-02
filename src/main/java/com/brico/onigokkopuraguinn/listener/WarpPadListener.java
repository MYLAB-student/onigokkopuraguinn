package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.Role;
import com.brico.onigokkopuraguinn.WarpPadCache;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 警察はエメラルド、泥棒はダイヤの床で別の同種ブロックへワープ。クールダウン30秒。
 */
public class WarpPadListener implements Listener {

    private static final long COOLDOWN_MILLIS = 30_000L;

    private static final Map<UUID, Long> cooldownUntil = new HashMap<>();

    public static void clearCooldowns() {
        cooldownUntil.clear();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        Block fromUnder = event.getFrom().getBlock().getRelative(BlockFace.DOWN);
        Block toUnder = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        // 足元ブロックが変わったときだけ
        if (fromUnder.getX() == toUnder.getX()
                && fromUnder.getY() == toUnder.getY()
                && fromUnder.getZ() == toUnder.getZ()) {
            return;
        }

        Material padType = toUnder.getType();
        if (padType != Material.EMERALD_BLOCK && padType != Material.DIAMOND_BLOCK) {
            return;
        }

        // パッド上から別パッドへ歩いた場合は無視（乗った瞬間のみ）
        if (fromUnder.getType() == Material.EMERALD_BLOCK
                || fromUnder.getType() == Material.DIAMOND_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Role role = GameManager.getInstance().getRole(player);
        if (role == null) return;

        // 警察 → エメラルド、泥棒 → ダイヤ
        if (role == Role.POLICE && padType != Material.EMERALD_BLOCK) return;
        if (role == Role.THIEF && padType != Material.DIAMOND_BLOCK) return;

        if (isOnCooldown(player)) {
            player.sendMessage("§e[ゲーム] ワープはクールダウン中です。（残り "
                    + remainingCooldownSeconds(player) + " 秒）");
            return;
        }

        Location destinationPad = WarpPadCache.getInstance()
                .pickRandomOther(toUnder.getLocation(), padType);
        if (destinationPad == null) {
            player.sendMessage("§c[ゲーム] ワープ先のブロックが見つかりませんでした。");
            return;
        }

        Location dest = destinationPad.clone().add(0.5, 1.0, 0.5);
        dest.setYaw(player.getLocation().getYaw());
        dest.setPitch(player.getLocation().getPitch());

        cooldownUntil.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_MILLIS);
        player.teleport(dest);
        player.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.sendMessage("§a[ゲーム] ワープしました！（クールダウン 30秒）");
    }

    private static boolean isOnCooldown(Player player) {
        Long until = cooldownUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    private static long remainingCooldownSeconds(Player player) {
        Long until = cooldownUntil.get(player.getUniqueId());
        if (until == null) return 0;
        return Math.max(0, (until - System.currentTimeMillis() + 999) / 1000);
    }
}

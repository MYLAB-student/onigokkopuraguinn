package com.brico.onigokkopuraguinn;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ゲーム用ボスバータイマー（20分）。0で警察の勝ち。
 */
public final class GameTimer {

    private static final int TOTAL_SECONDS = 20 * 60;

    private static Onigokkopuraguinn plugin;
    private static BossBar bossBar;
    private static BukkitTask task;
    private static int remainingSeconds;
    private static final Set<UUID> viewers = new HashSet<>();

    private GameTimer() {}

    public static void init(Onigokkopuraguinn pluginInstance) {
        plugin = pluginInstance;
    }

    public static void start(Collection<Player> players) {
        stop();
        if (plugin == null) return;

        remainingSeconds = TOTAL_SECONDS;
        bossBar = BossBar.bossBar(
                formatTitle(remainingSeconds),
                1f,
                BossBar.Color.BLUE,
                BossBar.Overlay.PROGRESS
        );

        for (Player player : players) {
            player.showBossBar(bossBar);
            viewers.add(player.getUniqueId());
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, GameTimer::tick, 20L, 20L);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (bossBar != null) {
            for (UUID uuid : viewers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.hideBossBar(bossBar);
                }
            }
            bossBar = null;
        }
        viewers.clear();
        remainingSeconds = 0;
    }

    private static void tick() {
        if (bossBar == null) return;

        // ゲームが終了していたら止める
        if (GameManager.getInstance().getRoles().isEmpty()) {
            stop();
            return;
        }

        remainingSeconds--;
        if (remainingSeconds < 0) {
            remainingSeconds = 0;
        }

        float progress = remainingSeconds / (float) TOTAL_SECONDS;
        bossBar.progress(Math.max(0f, Math.min(1f, progress)));
        bossBar.name(formatTitle(remainingSeconds));

        // 途中参加・再ログイン向けに参加者へ再表示
        syncViewers();

        if (remainingSeconds <= 0) {
            List<Player> participants = onlineParticipants();
            stop();
            if (!participants.isEmpty()) {
                GameEndHelper.policeWin(participants);
                for (Player player : participants) {
                    player.sendMessage("§9[ゲーム] 制限時間終了！警察の勝ちです！");
                }
            }
        }
    }

    private static void syncViewers() {
        if (bossBar == null) return;
        for (Player player : onlineParticipants()) {
            if (viewers.add(player.getUniqueId())) {
                player.showBossBar(bossBar);
            }
        }
    }

    private static List<Player> onlineParticipants() {
        List<Player> list = new ArrayList<>();
        Map<UUID, Role> roles = GameManager.getInstance().getRoles();
        for (UUID uuid : roles.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                list.add(player);
            }
        }
        return list;
    }

    private static Component formatTitle(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        String time = String.format("%d:%02d", minutes, secs);
        NamedTextColor color = seconds <= 60 ? NamedTextColor.RED
                : seconds <= 5 * 60 ? NamedTextColor.YELLOW
                : NamedTextColor.WHITE;
        return Component.text("残り時間 " + time, color);
    }
}

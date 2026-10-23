package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.Onigokkopuraguinn;
import com.brico.onigokkopuraguinn.command.GameStartCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * オンライン全員が苔むした丸石の上にいるとき、5秒カウントダウン後にゲーム開始。
 */
public final class MossyCobbleStartChecker {

    private static final int COUNTDOWN_SECONDS = 5;

    private static BukkitTask task;
    /** -1 = 待機中、5〜0 = カウントダウン中 */
    private static int countdown = -1;

    private MossyCobbleStartChecker() {}

    public static void start(Onigokkopuraguinn plugin) {
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, MossyCobbleStartChecker::tick, 20L, 20L);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        countdown = -1;
    }

    private static void tick() {
        // 試合中は発動しない
        if (!GameManager.getInstance().getRoles().isEmpty()) {
            cancelCountdown();
            return;
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty() || !allOnMossyCobble(players)) {
            cancelCountdown();
            return;
        }

        if (countdown < 0) {
            countdown = COUNTDOWN_SECONDS;
        }

        if (countdown > 0) {
            showCountdown(players, countdown);
            countdown--;
            return;
        }

        // 0 になったらスタート
        showCountdown(players, 0);
        countdown = -1;

        String error = GameStartCommand.startGame(players);
        if (error != null) {
            for (Player player : players) {
                player.sendMessage(error);
            }
        }
    }

    private static boolean allOnMossyCobble(List<Player> players) {
        for (Player player : players) {
            Material under = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
            if (under != Material.MOSSY_COBBLESTONE) {
                return false;
            }
        }
        return true;
    }

    private static void cancelCountdown() {
        if (countdown < 0) return;
        countdown = -1;
    }

    private static void showCountdown(List<Player> players, int seconds) {
        Component title = seconds == 0
                ? Component.text("スタート！", NamedTextColor.GREEN, TextDecoration.BOLD)
                : Component.text(String.valueOf(seconds), NamedTextColor.GOLD, TextDecoration.BOLD);
        Component subtitle = seconds == 0
                ? Component.empty()
                : Component.text("まもなくゲーム開始", NamedTextColor.YELLOW);

        Title display = Title.title(
                title,
                subtitle,
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ofMillis(200))
        );

        for (Player player : players) {
            player.showTitle(display);
        }
    }
}

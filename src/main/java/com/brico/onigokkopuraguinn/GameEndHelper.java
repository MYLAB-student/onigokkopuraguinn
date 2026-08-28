package com.brico.onigokkopuraguinn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;

/**
 * ゲーム終了時のタイトル表示とリスポーン地点への帰還。
 */
public final class GameEndHelper {

    private GameEndHelper() {}

    public static void endWithTitle(Collection<Player> players, String titleText, NamedTextColor color) {
        GameTimer.stop();

        // /gamereset と同じ：チェスト中身クリア＋役職解除
        GameManager.getInstance().resetGameState();

        Title title = Title.title(
                Component.text(titleText, color, TextDecoration.BOLD),
                Component.empty(),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(4), Duration.ofMillis(750))
        );

        for (Player player : players) {
            player.getInventory().clear();
            player.showTitle(title);
            Location spawn = player.getWorld().getSpawnLocation().clone().add(0.5, 0, 0.5);
            player.teleport(spawn);
        }
    }

    public static void thievesWin(Collection<Player> players) {
        endWithTitle(players, "泥棒の勝ち！", NamedTextColor.RED);
    }

    public static void policeWin(Collection<Player> players) {
        endWithTitle(players, "警察の勝ち！", NamedTextColor.BLUE);
    }
}

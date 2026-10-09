package com.brico.onigokkopuraguinn;

import com.brico.onigokkopuraguinn.listener.PlayerSessionListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.time.Duration;
import java.util.Collection;

/**
 * ゲーム終了時のタイトル表示とリスポーン地点への帰還。
 */
public final class GameEndHelper {

    private GameEndHelper() {}

    /**
     * 通常インベントリと、プレイヤー画面の2×2作業欄を空にする。
     *
     * @param clearCursor true ならマウスで掴んでいるアイテムも消す
     */
    public static void clearPlayerItems(Player player, boolean clearCursor) {
        player.getInventory().clear();
        Inventory top = player.getOpenInventory().getTopInventory();
        if (top.getType() == InventoryType.CRAFTING) {
            top.clear();
        }
        if (clearCursor) {
            player.setItemOnCursor(null);
        }
    }

    public static void endWithTitle(Collection<Player> players, String titleText, NamedTextColor color) {
        GameTimer.stop();

        // 試合中に壊されたひび割れた石レンガを復元
        GameManager.getInstance().restoreBrokenCrackedBricks();

        // /gamereset と同じ：チェスト中身クリア＋役職解除
        GameManager.getInstance().resetGameState();

        Title title = Title.title(
                Component.text(titleText, color, TextDecoration.BOLD),
                Component.empty(),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(4), Duration.ofMillis(750))
        );

        for (Player player : players) {
            clearPlayerItems(player, true);
            player.showTitle(title);
            player.teleport(PlayerSessionListener.lobbyLocation(player));
        }
    }

    public static void thievesWin(Collection<Player> players) {
        endWithTitle(players, "泥棒の勝ち！", NamedTextColor.RED);
    }

    public static void policeWin(Collection<Player> players) {
        endWithTitle(players, "警察の勝ち！", NamedTextColor.BLUE);
    }
}

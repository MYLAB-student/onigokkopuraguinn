package com.brico.onigokkopuraguinn.command;

import com.brico.onigokkopuraguinn.GameEndHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * ゲームクリア：対象プレイヤーをリスポーン地点へ戻す（泥棒の勝ち）。
 */
public class GameClearCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<Player> players = CommandMessages.targetPlayers(sender);

        if (players.isEmpty()) {
            CommandMessages.send(sender, "§c[ゲーム] 対象プレイヤーがいません。");
            return true;
        }

        GameEndHelper.thievesWin(players);

        CommandMessages.send(sender, "§a[ゲーム] クリア！ "
                + players.size() + " 人をリスポーン地点へ戻しました。");
        return true;
    }
}

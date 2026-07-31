package com.brico.onigokkopuraguinn.command;

import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * プレイヤー以外（コマンドブロック・コンソール）からの実行時も、
 * 周辺／全体のプレイヤーにメッセージが届くようにする。
 */
final class CommandMessages {

    private CommandMessages() {}

    /** コマンド実行対象となるプレイヤー一覧（同じワールド優先） */
    static List<Player> targetPlayers(CommandSender sender) {
        if (sender instanceof BlockCommandSender blockSender) {
            return new ArrayList<>(blockSender.getBlock().getWorld().getPlayers());
        }
        if (sender instanceof Player player) {
            return new ArrayList<>(player.getWorld().getPlayers());
        }
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    static void send(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.sendMessage(message);
            return;
        }

        for (Player player : targetPlayers(sender)) {
            player.sendMessage(message);
        }
    }
}

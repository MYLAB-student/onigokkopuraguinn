package com.brico.onigokkopuraguinn.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

/**
 * チャット・プレイヤーコマンドは OP のみ。
 * コマンドブロックの発動・ボタン操作は全員可能。
 */
public class OpRestrictListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        if (event.getPlayer().isOp()) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().isOp()) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage("§c[ゲーム] コマンドは OP のみ使用できます。");
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        if (event.getPlayer().isOp()) return;
        event.getCommands().clear();
    }
}

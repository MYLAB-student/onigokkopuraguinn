package com.brico.onigokkopuraguinn.command;

import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.GameTimer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GameResetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameTimer.stop();
        GameManager manager = GameManager.getInstance();

        int cleared = manager.resetGameState();
        int registered = manager.getChestLocations().size();

        CommandMessages.send(sender, "§a[ゲーム] リセット完了。"
                + cleared + " 個のチェストの中身を空にし、役職を解除しました。"
                + " §7(登録 " + registered + " 個はそのまま /gamestart で再利用できます)");
        return true;
    }
}

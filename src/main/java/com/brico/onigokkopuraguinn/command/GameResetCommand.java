package com.brico.onigokkopuraguinn.command;

import com.brico.onigokkopuraguinn.GameManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GameResetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameManager manager = GameManager.getInstance();
        List<Location> chests = manager.getChestLocations();

        int cleared = 0;
        for (Location loc : chests) {
            Block block = loc.getBlock();
            if (block.getState() instanceof Chest chest) {
                chest.getInventory().clear();
                cleared++;
            }
        }

        int registered = chests.size();
        manager.clearChests();

        sender.sendMessage("§a[ゲーム] リセット完了。"
                + "登録チェスト " + registered + " 個を解除し、"
                + cleared + " 個の中身を空にしました。");
        return true;
    }
}

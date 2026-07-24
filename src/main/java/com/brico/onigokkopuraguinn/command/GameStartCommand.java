package com.brico.onigokkopuraguinn.command;

import com.brico.onigokkopuraguinn.GameManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameStartCommand implements CommandExecutor {

    /** 配布するアイテム: 木の棒×2、鉄の延べ棒×3 */
    private static List<ItemStack> createGameItems() {
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemStack(Material.STICK));
        items.add(new ItemStack(Material.STICK));
        items.add(new ItemStack(Material.IRON_INGOT));
        items.add(new ItemStack(Material.IRON_INGOT));
        items.add(new ItemStack(Material.IRON_INGOT));
        return items;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameManager manager = GameManager.getInstance();
        int required = GameManager.requiredChestCount();

        if (manager.getChestLocations().size() < required) {
            sender.sendMessage("§c[ゲーム] チェストが足りません。"
                    + "必要数: " + required
                    + " / 登録数: " + manager.getChestLocations().size());
            return true;
        }

        List<ItemStack> items = createGameItems();
        Collections.shuffle(items);

        List<Location> selectedChests = manager.pickRandomChests(items.size());

        int distributed = 0;
        for (int i = 0; i < items.size(); i++) {
            Location loc = selectedChests.get(i);
            Block block = loc.getBlock();

            if (!(block.getState() instanceof Chest chest)) {
                sender.sendMessage("§c[ゲーム] チェストが見つかりませんでした: "
                        + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                continue;
            }

            Inventory inv = chest.getInventory();
            inv.clear();
            inv.setItem(0, items.get(i));
            distributed++;
        }

        sender.sendMessage("§a[ゲーム] ゲームスタート！ " + distributed + " 個のチェストにアイテムを配置しました。");
        return true;
    }
}

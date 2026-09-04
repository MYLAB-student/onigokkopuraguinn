package com.brico.onigokkopuraguinn.command;

import com.brico.onigokkopuraguinn.BlackoutDevice;
import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.GameTimer;
import com.brico.onigokkopuraguinn.PlayerFreeze;
import com.brico.onigokkopuraguinn.PoliceBaton;
import com.brico.onigokkopuraguinn.Role;
import com.brico.onigokkopuraguinn.ThiefSnowball;
import com.brico.onigokkopuraguinn.listener.AdventurePickaxeListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameStartCommand implements CommandExecutor {

    /** 泥棒のスポーン位置 */
    private static final double THIEF_X = 49;
    private static final double THIEF_Y = -60;
    private static final double THIEF_Z = -81;

    /** 警察のスポーン位置 */
    private static final double POLICE_X = 11;
    private static final double POLICE_Y = -60;
    private static final double POLICE_Z = -22;

    /** 配布するアイテム: 木の棒×2、鉄の延べ棒×3、トリップワイヤーフック×2、停電装置×2 */
    private static List<ItemStack> createGameItems() {
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemStack(Material.STICK));
        items.add(new ItemStack(Material.STICK));
        items.add(new ItemStack(Material.IRON_INGOT));
        items.add(new ItemStack(Material.IRON_INGOT));
        items.add(new ItemStack(Material.IRON_INGOT));
        items.add(new ItemStack(Material.TRIPWIRE_HOOK));
        items.add(new ItemStack(Material.TRIPWIRE_HOOK));
        items.add(BlackoutDevice.create());
        items.add(BlackoutDevice.create());
        return items;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameManager manager = GameManager.getInstance();
        List<Player> players = CommandMessages.targetPlayers(sender);

        if (players.isEmpty()) {
            CommandMessages.send(sender, "§c[ゲーム] 対象プレイヤーがいません。");
            return true;
        }

        // ブレイズロッドで選択中（緑発光）のチェストを自動登録
        int newlyRegistered = manager.registerSelectedChests();
        if (newlyRegistered > 0) {
            CommandMessages.send(sender, "§a[ゲーム] 選択中のチェストを " + newlyRegistered + " 個自動登録しました。");
        }

        int required = GameManager.requiredChestCount();
        if (manager.getChestLocations().size() < required) {
            CommandMessages.send(sender, "§c[ゲーム] チェストが足りません。"
                    + "必要数: " + required
                    + " / 登録数: " + manager.getChestLocations().size()
                    + " §7(ブレイズロッドでチェストを選択してください)");
            return true;
        }

        // 役職割り当て: ランダムで1人警察、それ以外泥棒
        Player police = manager.assignRoles(players);
        notifyRoles(players, police);
        teleportByRole(players);
        givePoliceBaton(police);
        giveThiefSnowballs(players);
        freezePoliceAtStart(police);

        for (Player player : players) {
            AdventurePickaxeListener.applyToPlayer(player);
        }

        List<ItemStack> items = createGameItems();
        Collections.shuffle(items);

        List<Location> selectedChests = manager.pickRandomChests(items.size());

        int distributed = 0;
        for (int i = 0; i < items.size(); i++) {
            Location loc = selectedChests.get(i);
            Block block = loc.getBlock();

            if (!(block.getState() instanceof Chest chest)) {
                CommandMessages.send(sender, "§c[ゲーム] チェストが見つかりませんでした: "
                        + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                continue;
            }

            Inventory inv = chest.getInventory();
            inv.clear();
            inv.setItem(0, items.get(i));
            distributed++;
        }

        CommandMessages.send(sender, "§a[ゲーム] ゲームスタート！ "
                + "警察 1人 / 泥棒 " + (players.size() - 1) + "人、"
                + "チェスト " + distributed + " 個にアイテムを配置しました。");

        GameTimer.start(players);
        return true;
    }

    private static void givePoliceBaton(Player police) {
        if (police == null) return;
        police.getInventory().addItem(PoliceBaton.create());
        police.sendMessage("§9[ゲーム] 警棒を受け取りました。泥棒を殴って捕まえよう！");
    }

    private static void giveThiefSnowballs(List<Player> players) {
        for (Player player : players) {
            if (GameManager.getInstance().getRole(player) != Role.THIEF) continue;
            player.getInventory().addItem(ThiefSnowball.create(ThiefSnowball.AMOUNT));
            player.sendMessage("§b[ゲーム] 凍結の雪玉を " + ThiefSnowball.AMOUNT
                    + " 個受け取りました。警察に当てると3秒間動けなくなります！");
        }
    }

    /** スタート時、警察（鬼）を15秒間動けなくする */
    private static void freezePoliceAtStart(Player police) {
        if (police == null) return;
        PlayerFreeze freeze = PlayerFreeze.getInstance();
        if (freeze == null) return;

        freeze.freeze(police, 15L * 20L, "§a[ゲーム] 待機時間が終わりました。追いかけ開始！");
        police.sendMessage("§e[ゲーム] スタートダッシュ猶予のため、15秒間動けません。");
        for (Player player : police.getWorld().getPlayers()) {
            if (player.equals(police)) continue;
            player.sendMessage("§e[ゲーム] 警察は15秒間動けません。逃げ支度をしよう！");
        }
    }

    private static void notifyRoles(List<Player> players, Player police) {
        if (police != null) {
            for (Player player : players) {
                player.sendMessage("§e[ゲーム] 警察は §9" + police.getName() + " §eです！");
            }
        }

        for (Player player : players) {
            Role role = GameManager.getInstance().getRole(player);
            if (role == null) continue;

            if (role == Role.POLICE) {
                player.sendMessage("§9[ゲーム] あなたは §l警察§r§9 です！泥棒を捕まえよう！");
            } else {
                player.sendMessage("§c[ゲーム] あなたは §l泥棒§r§c です！警察から逃げよう！");
            }
        }
    }

    private static void teleportByRole(List<Player> players) {
        for (Player player : players) {
            Role role = GameManager.getInstance().getRole(player);
            if (role == null) continue;

            World world = player.getWorld();
            Location destination = role == Role.POLICE
                    ? new Location(world, POLICE_X + 0.5, POLICE_Y, POLICE_Z + 0.5)
                    : new Location(world, THIEF_X + 0.5, THIEF_Y, THIEF_Z + 0.5);

            player.teleport(destination);
        }
    }
}

package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.Onigokkopuraguinn;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * 金ブロックに乗った状態でトリップワイヤーフックを使い、鉄扉を5秒間開ける。
 */
public class TripwireDoorListener implements Listener {

    private static final long OPEN_TICKS = 5L * 20L;

    private final Onigokkopuraguinn plugin;
    /** 扉ブロックキー → 自動閉鎖タスク */
    private final Map<String, BukkitTask> closeTasks = new HashMap<>();

    public TripwireDoorListener(Onigokkopuraguinn plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.TRIPWIRE_HOOK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.IRON_DOOR) return;

        if (!isStandingOnGoldBlock(player)) {
            player.sendMessage("§c[ゲーム] 金ブロックに乗っているときだけ鉄扉を開けられます。");
            return;
        }

        event.setCancelled(true);

        Block doorBlock = resolveDoorBlock(clicked);
        if (!(doorBlock.getBlockData() instanceof Openable)) {
            return;
        }

        // フックを1つ消費し、登録済みチェストのランダムな1つへ戻す
        hand.setAmount(hand.getAmount() - 1);
        boolean returned = GameManager.getInstance()
                .placeItemInRandomChest(new ItemStack(Material.TRIPWIRE_HOOK));

        openDoor(doorBlock);
        player.getWorld().playSound(doorBlock.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1f, 1f);
        if (returned) {
            player.sendMessage("§a[ゲーム] 鉄扉を5秒間開けました。トリップワイヤーフックはチェストに戻りました。");
        } else {
            player.sendMessage("§a[ゲーム] 鉄扉を5秒間開けました。§e(戻すチェストが見つかりませんでした)");
        }

        scheduleClose(doorBlock);
    }

    private static boolean isStandingOnGoldBlock(Player player) {
        Block under = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        return under.getType() == Material.GOLD_BLOCK;
    }

    private static Block resolveDoorBlock(Block block) {
        if (block.getBlockData() instanceof Bisected bisected
                && bisected.getHalf() == Bisected.Half.TOP) {
            return block.getRelative(BlockFace.DOWN);
        }
        return block;
    }

    private void openDoor(Block doorBlock) {
        if (!(doorBlock.getBlockData() instanceof Openable openable)) return;
        openable.setOpen(true);
        doorBlock.setBlockData(openable);
    }

    private void closeDoor(Block doorBlock) {
        if (doorBlock.getType() != Material.IRON_DOOR) return;
        if (!(doorBlock.getBlockData() instanceof Openable openable)) return;
        openable.setOpen(false);
        doorBlock.setBlockData(openable);
        doorBlock.getWorld().playSound(doorBlock.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1f);
    }

    private void scheduleClose(Block doorBlock) {
        String key = keyOf(doorBlock);

        BukkitTask previous = closeTasks.remove(key);
        if (previous != null) {
            previous.cancel();
        }

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            closeTasks.remove(key);
            closeDoor(doorBlock);
        }, OPEN_TICKS);

        closeTasks.put(key, task);
    }

    private static String keyOf(Block block) {
        return block.getWorld().getName() + ":"
                + block.getX() + "," + block.getY() + "," + block.getZ();
    }
}

package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameEndHelper;
import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.Onigokkopuraguinn;
import com.brico.onigokkopuraguinn.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 泥棒全員がクォーツブロックの上にいるとき、警察の勝ちとして終了する。
 */
public class PoliceWinChecker {

    private static BukkitTask task;

    private PoliceWinChecker() {}

    public static void start(Onigokkopuraguinn plugin) {
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, PoliceWinChecker::tick, 20L, 10L);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private static void tick() {
        GameManager manager = GameManager.getInstance();
        Map<UUID, Role> roles = manager.getRoles();
        if (roles.isEmpty()) return;

        List<Player> thieves = new ArrayList<>();
        List<Player> participants = new ArrayList<>();
        int assignedThieves = 0;

        for (Map.Entry<UUID, Role> entry : roles.entrySet()) {
            if (entry.getValue() == Role.THIEF) {
                assignedThieves++;
            }
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                continue;
            }
            participants.add(player);
            if (entry.getValue() == Role.THIEF) {
                thieves.add(player);
            }
        }

        // 泥棒が1人もいない、またはオフラインの泥棒がいる場合は未達成
        if (thieves.isEmpty() || thieves.size() != assignedThieves) {
            return;
        }

        for (Player thief : thieves) {
            Material under = thief.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
            if (under != Material.QUARTZ_BLOCK) {
                return;
            }
        }

        GameEndHelper.policeWin(participants);
        for (Player player : participants) {
            player.sendMessage("§9[ゲーム] 泥棒全員がクォーツブロックに乗ったため、警察の勝ちです！");
        }
    }
}

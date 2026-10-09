package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.GameEndHelper;
import com.brico.onigokkopuraguinn.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * ログアウトで役職解除、再ログインで持ち物クリア・アドベンチャー・固定リスポーンへ帰還。
 */
public class PlayerSessionListener implements Listener {

    private static final double RESPAWN_X = 1;
    private static final double RESPAWN_Y = -60;
    private static final double RESPAWN_Z = 2;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GameManager.getInstance().removeRole(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameEndHelper.clearPlayerItems(player, false);
        applyJoinDefaults(player);
        // ログアウト位置に関わらず固定リスポーンへ
        player.teleport(lobbyLocation(player));
    }

    public static void applyJoinDefaults(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setRespawnLocation(lobbyLocation(player), true);
    }

    public static Location lobbyLocation(Player player) {
        World world = null;
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                world = w;
                break;
            }
        }
        if (world == null) {
            world = player.getWorld();
        }
        return new Location(
                world,
                RESPAWN_X + 0.5,
                RESPAWN_Y,
                RESPAWN_Z + 0.5
        );
    }
}

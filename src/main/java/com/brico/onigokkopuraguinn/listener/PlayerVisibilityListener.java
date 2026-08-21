package com.brico.onigokkopuraguinn.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * ネームプレートとロケーターバーから他プレイヤーの位置が見えないようにする。
 */
public class PlayerVisibilityListener implements Listener {

    private static final String TEAM_NAME = "og_hide_name";

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        hidePlayer(event.getPlayer());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        disableLocatorBar(event.getWorld());
    }

    public static void applyAll() {
        for (World world : Bukkit.getWorlds()) {
            disableLocatorBar(world);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            hidePlayer(player);
        }
    }

    public static void hidePlayer(Player player) {
        hideNameTag(player);
        hideFromLocatorBar(player);
    }

    public static void disableLocatorBar(World world) {
        world.setGameRule(GameRules.LOCATOR_BAR, false);
    }

    private static void hideNameTag(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(TEAM_NAME);
        if (team == null) {
            team = board.registerNewTeam(TEAM_NAME);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
        team.addEntry(player.getName());
    }

    private static void hideFromLocatorBar(Player player) {
        AttributeInstance transmit = player.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE);
        if (transmit != null) {
            transmit.setBaseValue(0);
        }
        AttributeInstance receive = player.getAttribute(Attribute.WAYPOINT_RECEIVE_RANGE);
        if (receive != null) {
            receive.setBaseValue(0);
        }
    }
}

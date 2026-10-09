package com.brico.onigokkopuraguinn.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * ワールドを常に昼に固定する。
 */
public class AlwaysDayListener implements Listener {

    /** 正午 */
    private static final long NOON = 6000L;

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        apply(event.getWorld());
    }

    public static void applyAll() {
        for (World world : Bukkit.getWorlds()) {
            apply(world);
        }
    }

    public static void apply(World world) {
        // Paper のバージョン差（doDaylightCycle / advanceTime）に両対応
        GameRule<Boolean> rule = findBooleanRule("advanceTime", "doDaylightCycle");
        if (rule != null) {
            world.setGameRule(rule, false);
        }

        // ネザー等はワールド時計がなく setTime が例外になる
        try {
            world.setTime(NOON);
        } catch (IllegalArgumentException ignored) {
            // 時計のないディメンションはスキップ
        }
    }

    @SuppressWarnings("unchecked")
    private static GameRule<Boolean> findBooleanRule(String... names) {
        for (String name : names) {
            GameRule<?> rule = GameRule.getByName(name);
            if (rule != null && rule.getType() == Boolean.class) {
                return (GameRule<Boolean>) rule;
            }
        }
        return null;
    }
}

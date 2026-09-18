package com.brico.onigokkopuraguinn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 泥棒の被弾回数・牢屋状態・警棒無敵を管理する。
 */
public final class ThiefCatchState {

    /** 泥棒の初期スポーン */
    public static final double THIEF_SPAWN_X = 49;
    public static final double THIEF_SPAWN_Y = -60;
    public static final double THIEF_SPAWN_Z = -81;

    /** 牢屋 */
    public static final double JAIL_X = 36;
    public static final double JAIL_Y = -60;
    public static final double JAIL_Z = -82;

    /** 牢屋脱出判定の距離（ブロック） */
    private static final double JAIL_ESCAPE_DISTANCE = 4.0;

    private static final long BATON_IMMUNITY_MILLIS = 5_000L;

    private static final Map<UUID, Integer> hitCounts = new HashMap<>();
    private static final Map<UUID, Long> batonImmuneUntil = new HashMap<>();
    private static final Set<UUID> inJail = new HashSet<>();

    private ThiefCatchState() {}

    public static void clearAll() {
        hitCounts.clear();
        batonImmuneUntil.clear();
        inJail.clear();
    }

    public static int getHitCount(Player player) {
        return hitCounts.getOrDefault(player.getUniqueId(), 0);
    }

    public static int incrementHit(Player player) {
        int next = getHitCount(player) + 1;
        hitCounts.put(player.getUniqueId(), next);
        return next;
    }

    public static void resetHits(Player player) {
        hitCounts.remove(player.getUniqueId());
        inJail.remove(player.getUniqueId());
    }

    public static void grantBatonImmunity(Player player) {
        batonImmuneUntil.put(player.getUniqueId(), System.currentTimeMillis() + BATON_IMMUNITY_MILLIS);
    }

    public static boolean isBatonImmune(Player player) {
        Long until = batonImmuneUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public static long remainingImmunitySeconds(Player player) {
        Long until = batonImmuneUntil.get(player.getUniqueId());
        if (until == null) return 0;
        return Math.max(0, (until - System.currentTimeMillis() + 999) / 1000);
    }

    public static void markInJail(Player player) {
        inJail.add(player.getUniqueId());
    }

    public static boolean isInJail(Player player) {
        return inJail.contains(player.getUniqueId());
    }

    public static Location thiefSpawn(World world) {
        return new Location(world, THIEF_SPAWN_X + 0.5, THIEF_SPAWN_Y, THIEF_SPAWN_Z + 0.5);
    }

    public static Location jail(World world) {
        return new Location(world, JAIL_X + 0.5, JAIL_Y, JAIL_Z + 0.5);
    }

    /**
     * 牢屋から十分離れたら脱出としてヒット回数をリセットする。
     *
     * @return 脱出した場合 true
     */
    public static boolean tryEscapeJail(Player player) {
        if (!isInJail(player)) return false;

        Location jailLoc = jail(player.getWorld());
        if (player.getLocation().distanceSquared(jailLoc) < JAIL_ESCAPE_DISTANCE * JAIL_ESCAPE_DISTANCE) {
            return false;
        }

        resetHits(player);
        return true;
    }
}

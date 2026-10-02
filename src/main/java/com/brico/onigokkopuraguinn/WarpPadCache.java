package com.brico.onigokkopuraguinn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * エメラルド／ダイヤのワープ床の座標キャッシュ。
 */
public final class WarpPadCache {

    private static final WarpPadCache INSTANCE = new WarpPadCache();

    private final Set<String> emeraldPads = new HashSet<>();
    private final Set<String> diamondPads = new HashSet<>();
    private final Map<String, Location> locations = new HashMap<>();

    private WarpPadCache() {}

    public static WarpPadCache getInstance() {
        return INSTANCE;
    }

    public void clear() {
        emeraldPads.clear();
        diamondPads.clear();
        locations.clear();
    }

    public void register(Location location, Material type) {
        if (type != Material.EMERALD_BLOCK && type != Material.DIAMOND_BLOCK) return;
        Location blockLoc = location.toBlockLocation();
        String key = keyOf(blockLoc);
        locations.put(key, blockLoc);
        if (type == Material.EMERALD_BLOCK) {
            emeraldPads.add(key);
        } else {
            diamondPads.add(key);
        }
    }

    public List<Location> getOtherPads(Location from, Material type) {
        Set<String> pads = type == Material.EMERALD_BLOCK ? emeraldPads : diamondPads;
        List<Location> result = new ArrayList<>();
        String fromKey = keyOf(from.toBlockLocation());
        World world = from.getWorld();

        for (String key : pads) {
            if (key.equals(fromKey)) continue;
            Location loc = locations.get(key);
            if (loc == null || loc.getWorld() == null) continue;
            if (world != null && !world.equals(loc.getWorld())) continue;
            if (loc.getBlock().getType() != type) continue;
            result.add(loc);
        }
        return result;
    }

    /**
     * 周囲を走査して同種のワープ床をキャッシュに追加する。
     */
    public void discoverAround(Location center, Material type) {
        World world = center.getWorld();
        if (world == null) return;

        int radius = 160;
        int yRange = 8;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                for (int y = cy - yRange; y <= cy + yRange; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == type) {
                        register(block.getLocation(), type);
                    }
                }
            }
        }
    }

    public Location pickRandomOther(Location from, Material type) {
        register(from, type);
        List<Location> others = getOtherPads(from, type);
        if (others.isEmpty()) {
            discoverAround(from, type);
            others = getOtherPads(from, type);
        }
        if (others.isEmpty()) return null;
        Collections.shuffle(others);
        return others.get(0);
    }

    private static String keyOf(Location location) {
        World world = location.getWorld();
        String worldName = world != null ? world.getName() : "null";
        return worldName + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
}

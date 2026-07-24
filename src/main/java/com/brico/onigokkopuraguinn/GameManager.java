package com.brico.onigokkopuraguinn;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameManager {

    private static final GameManager INSTANCE = new GameManager();

    private final List<Location> chestLocations = new ArrayList<>();

    private GameManager() {}

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public List<Location> getChestLocations() {
        return chestLocations;
    }

    public boolean addChest(Location location) {
        for (Location loc : chestLocations) {
            if (loc.getBlockX() == location.getBlockX()
                    && loc.getBlockY() == location.getBlockY()
                    && loc.getBlockZ() == location.getBlockZ()
                    && loc.getWorld() != null
                    && loc.getWorld().equals(location.getWorld())) {
                return false;
            }
        }
        chestLocations.add(location.toBlockLocation());
        return true;
    }

    public void clearChests() {
        chestLocations.clear();
    }

    /** ゲームで使うアイテム数（木の棒×2 + 鉄の延べ棒×3） */
    public static int requiredChestCount() {
        return 5;
    }

    /** 登録済みチェストをシャッフルして先頭 N 件を返す */
    public List<Location> pickRandomChests(int count) {
        List<Location> copy = new ArrayList<>(chestLocations);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(count, copy.size()));
    }
}

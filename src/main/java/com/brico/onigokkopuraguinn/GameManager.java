package com.brico.onigokkopuraguinn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private static final GameManager INSTANCE = new GameManager();

    private final List<Location> chestLocations = new ArrayList<>();
    private final Map<UUID, Role> roles = new HashMap<>();

    private GameManager() {}

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public List<Location> getChestLocations() {
        return chestLocations;
    }

    public Role getRole(Player player) {
        return roles.get(player.getUniqueId());
    }

    public Map<UUID, Role> getRoles() {
        return Collections.unmodifiableMap(roles);
    }

    /**
     * オンラインプレイヤーからランダムで1人を警察、それ以外を泥棒にする。
     *
     * @return 警察になったプレイヤー。プレイヤーが0人なら null
     */
    public Player assignRoles(List<Player> players) {
        roles.clear();
        if (players.isEmpty()) {
            return null;
        }

        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);

        Player police = shuffled.get(0);
        roles.put(police.getUniqueId(), Role.POLICE);

        for (int i = 1; i < shuffled.size(); i++) {
            roles.put(shuffled.get(i).getUniqueId(), Role.THIEF);
        }
        return police;
    }

    public void clearRoles() {
        roles.clear();
    }

    public boolean addChest(Location location) {
        if (contains(location)) {
            return false;
        }
        Location blockLoc = location.toBlockLocation();
        chestLocations.add(blockLoc);
        ChestHighlightManager highlight = ChestHighlightManager.getInstance();
        if (highlight != null) {
            highlight.highlight(blockLoc);
        }
        return true;
    }

    public boolean contains(Location location) {
        for (Location loc : chestLocations) {
            if (loc.getBlockX() == location.getBlockX()
                    && loc.getBlockY() == location.getBlockY()
                    && loc.getBlockZ() == location.getBlockZ()
                    && loc.getWorld() != null
                    && loc.getWorld().equals(location.getWorld())) {
                return true;
            }
        }
        return false;
    }

    /**
     * ブレイズロッドで選択中（緑発光）のチェストを GameManager に自動登録する。
     *
     * @return 新規に登録した件数
     */
    public int registerSelectedChests() {
        ChestHighlightManager highlight = ChestHighlightManager.getInstance();
        if (highlight == null) {
            return 0;
        }

        int added = 0;
        for (Location loc : highlight.findSelectedChestLocations()) {
            Block block = loc.getBlock();
            if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
                continue;
            }
            if (addChest(loc)) {
                added++;
            }
        }
        return added;
    }

    /** 登録済みチェストの中身だけ空にする（登録・発光は残す） */
    public int clearChestContents() {
        int cleared = 0;
        for (Location loc : chestLocations) {
            Block block = loc.getBlock();
            if (block.getState() instanceof Chest chest) {
                chest.getInventory().clear();
                cleared++;
            }
        }
        return cleared;
    }

    /** 登録と発光をすべて解除する */
    public void clearChests() {
        ChestHighlightManager highlight = ChestHighlightManager.getInstance();
        if (highlight != null) {
            highlight.clearAll();
        }
        chestLocations.clear();
    }

    /** ゲームで使うアイテム数（木の棒×2 + 鉄の延べ棒×3 + トリップワイヤーフック×1） */
    public static int requiredChestCount() {
        return 6;
    }

    /** 登録済みチェストをシャッフルして先頭 N 件を返す */
    public List<Location> pickRandomChests(int count) {
        List<Location> copy = new ArrayList<>(chestLocations);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(count, copy.size()));
    }
}

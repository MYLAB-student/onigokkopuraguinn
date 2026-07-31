package com.brico.onigokkopuraguinn;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ブレイズロッドで選択したチェストを緑色の発光アウトラインで表示する。
 */
public class ChestHighlightManager {

    private static final String TEAM_NAME = "og_chest_glow";
    private static final String PDC_KEY = "chest_select";

    private static ChestHighlightManager instance;

    private final Onigokkopuraguinn plugin;
    private final NamespacedKey selectKey;

    /** チェスト座標キー → ハイライト用エンティティ UUID */
    private final Map<String, UUID> highlights = new HashMap<>();

    public ChestHighlightManager(Onigokkopuraguinn plugin) {
        this.plugin = plugin;
        this.selectKey = new NamespacedKey(plugin, PDC_KEY);
        instance = this;
    }

    public static ChestHighlightManager getInstance() {
        return instance;
    }

    public void highlight(Location location) {
        String key = keyOf(location);
        if (highlights.containsKey(key)) return;

        World world = location.getWorld();
        if (world == null) return;

        // 既存のマーカー（リロード後など）があれば再利用
        for (Entity nearby : world.getNearbyEntities(location.toBlockLocation().add(0.5, 0.5, 0.5), 0.6, 0.6, 0.6)) {
            if (isHighlightEntity(nearby)) {
                highlights.put(key, nearby.getUniqueId());
                nearby.setGlowing(true);
                glowTeam().addEntity(nearby);
                return;
            }
        }

        Location spawnLoc = location.toBlockLocation();
        Shulker shulker = world.spawn(spawnLoc, Shulker.class, s -> {
            s.setAI(false);
            s.setAware(false);
            s.setSilent(true);
            s.setInvisible(true);
            s.setInvulnerable(true);
            s.setGravity(false);
            s.setPersistent(true);
            s.setCollidable(false);
            s.setGlowing(true);
            s.setPeek(0f);
            s.setRemoveWhenFarAway(false);
            s.setCanPickupItems(false);
            s.getPersistentDataContainer().set(selectKey, PersistentDataType.BYTE, (byte) 1);
        });

        glowTeam().addEntity(shulker);
        highlights.put(key, shulker.getUniqueId());
    }

    public void remove(Location location) {
        UUID uuid = highlights.remove(keyOf(location));
        if (uuid == null) return;
        removeEntity(uuid);
    }

    public void clearAll() {
        // メモリ上のマーカー
        Iterator<Map.Entry<String, UUID>> it = highlights.entrySet().iterator();
        while (it.hasNext()) {
            removeEntity(it.next().getValue());
            it.remove();
        }
        // ワールドに残ったマーカーも掃除
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (isHighlightEntity(entity)) {
                    glowTeam().removeEntity(entity);
                    entity.remove();
                }
            }
        }
    }

    public boolean isHighlightEntity(Entity entity) {
        if (highlights.containsValue(entity.getUniqueId())) {
            return true;
        }
        return entity.getPersistentDataContainer().has(selectKey, PersistentDataType.BYTE);
    }

    /**
     * ワールド上の選択マーカー（緑発光）からチェスト座標一覧を取得する。
     * プラグインリロード後でも、残っているマーカーから復元できる。
     */
    public List<Location> findSelectedChestLocations() {
        List<Location> result = new ArrayList<>();
        Map<String, Location> unique = new HashMap<>();

        // メモリ上
        for (Map.Entry<String, UUID> entry : highlights.entrySet()) {
            Entity entity = Bukkit.getEntity(entry.getValue());
            if (entity != null) {
                Location blockLoc = entity.getLocation().toBlockLocation();
                unique.put(keyOf(blockLoc), blockLoc);
            } else {
                Location parsed = parseKey(entry.getKey());
                if (parsed != null) {
                    unique.put(entry.getKey(), parsed);
                }
            }
        }

        // ワールド走査（リロード後の復元用）
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!isHighlightEntity(entity)) continue;
                Location blockLoc = entity.getLocation().toBlockLocation();
                String key = keyOf(blockLoc);
                unique.putIfAbsent(key, blockLoc);
                highlights.putIfAbsent(key, entity.getUniqueId());
                glowTeam().addEntity(entity);
                entity.setGlowing(true);
            }
        }

        result.addAll(unique.values());
        return result;
    }

    private void removeEntity(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity != null) {
            glowTeam().removeEntity(entity);
            entity.remove();
        }
    }

    private Team glowTeam() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(TEAM_NAME);
        if (team == null) {
            team = board.registerNewTeam(TEAM_NAME);
            team.color(NamedTextColor.GREEN);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        return team;
    }

    private static String keyOf(Location location) {
        World world = location.getWorld();
        String worldName = world != null ? world.getName() : "null";
        return worldName + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private static Location parseKey(String key) {
        int colon = key.indexOf(':');
        if (colon < 0) return null;
        String worldName = key.substring(0, colon);
        String[] parts = key.substring(colon + 1).split(",");
        if (parts.length != 3) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

package com.brico.onigokkopuraguinn;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * プレイヤーを一定時間動けなくする。
 */
public final class PlayerFreeze implements Listener {

    private static PlayerFreeze instance;

    private final JavaPlugin plugin;
    private final NamespacedKey freezeModifierKey;
    private final Map<UUID, Integer> freezeTaskIds = new HashMap<>();

    public PlayerFreeze(JavaPlugin plugin) {
        this.plugin = plugin;
        this.freezeModifierKey = new NamespacedKey(plugin, "freeze");
        instance = this;
    }

    public static PlayerFreeze getInstance() {
        return instance;
    }

    public void freeze(Player player, long ticks, String endMessage) {
        UUID id = player.getUniqueId();

        Integer previous = freezeTaskIds.remove(id);
        if (previous != null) {
            plugin.getServer().getScheduler().cancelTask(previous);
        }

        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(freezeModifierKey);
            AttributeModifier modifier = new AttributeModifier(
                    freezeModifierKey,
                    -1.0,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            speed.addTransientModifier(modifier);
        }

        int taskId = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            freezeTaskIds.remove(id);
            if (!player.isOnline()) return;
            AttributeInstance current = player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (current != null) {
                current.removeModifier(freezeModifierKey);
            }
            if (endMessage != null && !endMessage.isEmpty()) {
                player.sendMessage(endMessage);
            }
        }, ticks).getTaskId();

        freezeTaskIds.put(id, taskId);
    }

    public boolean isFrozen(Player player) {
        return freezeTaskIds.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isFrozen(event.getPlayer())) return;
        if (event.getTo() == null) return;

        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setTo(event.getFrom());
        }
    }
}

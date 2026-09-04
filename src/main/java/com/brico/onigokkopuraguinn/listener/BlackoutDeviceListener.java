package com.brico.onigokkopuraguinn.listener;

import com.brico.onigokkopuraguinn.BlackoutDevice;
import com.brico.onigokkopuraguinn.GameManager;
import com.brico.onigokkopuraguinn.Onigokkopuraguinn;
import com.brico.onigokkopuraguinn.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 停電装置：警察を30秒盲目にし、走れなくする。使い切りでチェストへ戻る。
 */
public class BlackoutDeviceListener implements Listener {

    private static final long EFFECT_TICKS = 30L * 20L;

    private final Onigokkopuraguinn plugin;
    private final Set<UUID> noSprintPlayers = new HashSet<>();

    public BlackoutDeviceListener(Onigokkopuraguinn plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player user = event.getPlayer();
        ItemStack hand = user.getInventory().getItemInMainHand();
        if (!BlackoutDevice.isBlackoutDevice(hand)) return;

        event.setCancelled(true);

        GameManager manager = GameManager.getInstance();
        List<Player> policeList = new ArrayList<>();
        for (Map.Entry<UUID, Role> entry : manager.getRoles().entrySet()) {
            if (entry.getValue() != Role.POLICE) continue;
            Player police = Bukkit.getPlayer(entry.getKey());
            if (police != null && police.isOnline()) {
                policeList.add(police);
            }
        }

        if (policeList.isEmpty()) {
            user.sendMessage("§c[ゲーム] 効果を与える警察がいません。");
            return;
        }

        // 使い切り：手元から消す
        hand.setAmount(hand.getAmount() - 1);

        // ランダムなチェストへ戻す（名前付きのまま）
        boolean returned = manager.placeItemInRandomChest(BlackoutDevice.create());

        PotionEffect blindness = new PotionEffect(
                PotionEffectType.BLINDNESS,
                (int) EFFECT_TICKS,
                0,
                false,
                true,
                true
        );

        for (Player police : policeList) {
            police.removePotionEffect(PotionEffectType.NIGHT_VISION);
            police.addPotionEffect(blindness);
            police.setSprinting(false);
            noSprintPlayers.add(police.getUniqueId());
            police.sendMessage("§5[ゲーム] 停電装置の効果で30秒間見えにくく、走れなくなりました！");
        }

        // 30秒後に走行制限を解除し、暗視を戻す（盲目はポーションで自動解除）
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player police : policeList) {
                noSprintPlayers.remove(police.getUniqueId());
                if (!police.isOnline()) continue;
                NightVisionListener.applyNightVision(police);
                police.sendMessage("§a[ゲーム] 停電装置の効果が切れました。");
            }
        }, EFFECT_TICKS);

        if (returned) {
            user.sendMessage("§5[ゲーム] 停電装置を使用しました。警察の視界を30秒奪いました。（チェストに戻りました）");
        } else {
            user.sendMessage("§5[ゲーム] 停電装置を使用しました。§e(戻すチェストが見つかりませんでした)");
        }
    }

    @EventHandler
    public void onSprint(PlayerToggleSprintEvent event) {
        if (!event.isSprinting()) return;
        if (!noSprintPlayers.contains(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        event.getPlayer().setSprinting(false);
    }
}

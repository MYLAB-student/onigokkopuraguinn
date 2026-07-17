package com.brico.onigokkopuraguinn.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightVisionListener implements Listener {

    private static final PotionEffect NIGHT_VISION = new PotionEffect(
            PotionEffectType.NIGHT_VISION,
            Integer.MAX_VALUE,
            0,
            false,
            false,
            false
    );

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyNightVision(event.getPlayer());
    }

    public static void applyNightVision(Player player) {
        player.addPotionEffect(NIGHT_VISION);
    }
}

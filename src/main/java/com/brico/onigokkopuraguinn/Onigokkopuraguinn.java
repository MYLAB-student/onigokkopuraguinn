package com.brico.onigokkopuraguinn;

import com.brico.onigokkopuraguinn.listener.NightVisionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Onigokkopuraguinn extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new NightVisionListener(), this);

        // オンライン中のプレイヤーにも付与（再起動後など）
        Bukkit.getOnlinePlayers().forEach(NightVisionListener::applyNightVision);
    }

    @Override
    public void onDisable() {
    }
}

package com.brico.onigokkopuraguinn;

import com.brico.onigokkopuraguinn.command.GameResetCommand;
import com.brico.onigokkopuraguinn.command.GameStartCommand;
import com.brico.onigokkopuraguinn.listener.ChestSetListener;
import com.brico.onigokkopuraguinn.listener.NightVisionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Onigokkopuraguinn extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new NightVisionListener(), this);
        getServer().getPluginManager().registerEvents(new ChestSetListener(), this);

        var cmd = getCommand("gamestart");
        if (cmd != null) {
            cmd.setExecutor(new GameStartCommand());
        }

        var resetCmd = getCommand("gamereset");
        if (resetCmd != null) {
            resetCmd.setExecutor(new GameResetCommand());
        }

        // オンライン中のプレイヤーにも付与（再起動後など）
        Bukkit.getOnlinePlayers().forEach(NightVisionListener::applyNightVision);
    }

    @Override
    public void onDisable() {
    }
}

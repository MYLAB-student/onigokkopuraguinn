package com.brico.onigokkopuraguinn;

import com.brico.onigokkopuraguinn.command.GameResetCommand;
import com.brico.onigokkopuraguinn.command.GameStartCommand;
import com.brico.onigokkopuraguinn.listener.AdventurePickaxeListener;
import com.brico.onigokkopuraguinn.listener.ChestSetListener;
import com.brico.onigokkopuraguinn.listener.HighlightProtectListener;
import com.brico.onigokkopuraguinn.listener.NightVisionListener;
import com.brico.onigokkopuraguinn.listener.NoHungerListener;
import com.brico.onigokkopuraguinn.listener.PlayerVisibilityListener;
import com.brico.onigokkopuraguinn.listener.PoliceBatonListener;
import com.brico.onigokkopuraguinn.listener.TripwireDoorListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Onigokkopuraguinn extends JavaPlugin {

    @Override
    public void onEnable() {
        new ChestHighlightManager(this);
        PoliceBaton.init(this);

        // リロード後などに残っている選択マーカーから復元
        GameManager.getInstance().registerSelectedChests();

        getServer().getPluginManager().registerEvents(new NightVisionListener(), this);
        getServer().getPluginManager().registerEvents(new NoHungerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerVisibilityListener(), this);
        getServer().getPluginManager().registerEvents(new ChestSetListener(), this);
        getServer().getPluginManager().registerEvents(new HighlightProtectListener(), this);
        getServer().getPluginManager().registerEvents(new TripwireDoorListener(this), this);
        getServer().getPluginManager().registerEvents(new AdventurePickaxeListener(), this);
        getServer().getPluginManager().registerEvents(new PoliceBatonListener(), this);

        // 権限デフォルト true ＋ 明示設定でコマンドブロックからも実行可能にする
        var cmd = getCommand("gamestart");
        if (cmd != null) {
            cmd.setExecutor(new GameStartCommand());
            cmd.setPermission("onigokkopuraguinn.gamestart");
        }

        var resetCmd = getCommand("gamereset");
        if (resetCmd != null) {
            resetCmd.setExecutor(new GameResetCommand());
            resetCmd.setPermission("onigokkopuraguinn.gamereset");
        }

        // オンライン中のプレイヤーにも付与（再起動後など）
        Bukkit.getOnlinePlayers().forEach(NightVisionListener::applyNightVision);
        Bukkit.getOnlinePlayers().forEach(NoHungerListener::fillHunger);
        Bukkit.getOnlinePlayers().forEach(AdventurePickaxeListener::applyToPlayer);
        PlayerVisibilityListener.applyAll();
    }

    @Override
    public void onDisable() {
        // 選択マーカーはワールドに残し、再有効化時・/gamestart 時に自動登録へ使う
    }
}

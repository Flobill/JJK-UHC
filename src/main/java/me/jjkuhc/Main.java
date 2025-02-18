package me.jjkuhc;

import me.jjkuhc.jjkcompass.CompassCommand;
import me.jjkuhc.jjkcompass.CompassGUI;
import me.jjkuhc.jjkcompass.CompassManager;
import me.jjkuhc.jjkcompass.SetCompassCommand;
import me.jjkuhc.jjkconfig.ConfigMenu;
import me.jjkuhc.jjkgame.GameCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("JJK UHC Plugin activé !");
        getCommand("jjk").setExecutor(new me.jjkuhc.commands.JJKCommand());
        getServer().getPluginManager().registerEvents(new ConfigMenu(), this);
        getCommand("spawn").setExecutor(new CompassCommand(this));
        getCommand("jump").setExecutor(new CompassCommand(this));
        getCommand("setspawn").setExecutor(new SetCompassCommand(this));
        getCommand("setjump").setExecutor(new SetCompassCommand(this));
        getServer().getPluginManager().registerEvents(new CompassManager(this), this);
        getServer().getPluginManager().registerEvents(new CompassGUI(this), this);
        getCommand("gameinfo").setExecutor(new GameCommand());
    }

    @Override
    public void onDisable() {
        getLogger().info("JJK UHC Plugin désactivé !");
    }
}
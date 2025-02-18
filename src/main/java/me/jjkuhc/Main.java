package me.jjkuhc;

import me.jjkuhc.jjkconfig.ConfigMenu;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("JJK UHC Plugin activé !");
        getCommand("jjk").setExecutor(new me.jjkuhc.commands.JJKCommand());
        getServer().getPluginManager().registerEvents(new ConfigMenu(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("JJK UHC Plugin désactivé !");
    }
}
package me.jjkuhc;

import me.jjkuhc.jjkcompass.CompassCommand;
import me.jjkuhc.jjkcompass.CompassGUI;
import me.jjkuhc.jjkcompass.CompassManager;
import me.jjkuhc.jjkcompass.SetCompassCommand;
import me.jjkuhc.jjkconfig.BorderConfigMenu;
import me.jjkuhc.jjkconfig.ConfigMenu;
import me.jjkuhc.jjkconfig.StartGameMenu;
import me.jjkuhc.jjkgame.GameCommand;
import me.jjkuhc.jjkgame.GameStartCommand;
import me.jjkuhc.scoreboard.ScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        getLogger().info("JJK UHC Plugin activé !");

        scoreboardManager = new ScoreboardManager(this);

        getServer().getPluginManager().registerEvents(new BorderConfigMenu(), this);
        getServer().getPluginManager().registerEvents(new StartGameMenu(), this);


        getCommand("jjk").setExecutor(new me.jjkuhc.commands.JJKCommand());
        getCommand("spawn").setExecutor(new CompassCommand(this));
        getCommand("jump").setExecutor(new CompassCommand(this));
        getCommand("setspawn").setExecutor(new SetCompassCommand(this));
        getCommand("setjump").setExecutor(new SetCompassCommand(this));
        getServer().getPluginManager().registerEvents(new CompassManager(this), this);
        getServer().getPluginManager().registerEvents(new CompassGUI(this), this);
        getCommand("gameinfo").setExecutor(new GameCommand());
        getCommand("jjkstart").setExecutor(new GameStartCommand(this, scoreboardManager));

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ConfigMenu(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("JJK UHC Plugin désactivé !");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scoreboardManager.setScoreboard(event.getPlayer());
    }
}
package me.jjkuhc;

import me.jjkuhc.commands.JJKCommand;
import me.jjkuhc.jjkcompass.CompassCommand;
import me.jjkuhc.jjkcompass.CompassGUI;
import me.jjkuhc.jjkcompass.CompassManager;
import me.jjkuhc.jjkcompass.SetCompassCommand;
import me.jjkuhc.jjkconfig.ConfigMenu;
import me.jjkuhc.jjkconfig.TimerConfigMenu; // Assurer l'import
import me.jjkuhc.jjkgame.GameCommand;
import me.jjkuhc.jjkgame.GameStartCommand;
import me.jjkuhc.scoreboard.ScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private ScoreboardManager scoreboardManager;
    private TimerConfigMenu timerConfigMenu;

    @Override
    public void onEnable() {
        getLogger().info("JJK UHC Plugin activé !");

        scoreboardManager = new ScoreboardManager(this);
        timerConfigMenu = new TimerConfigMenu();

        // Enregistrement des commandes
        getCommand("jjk").setExecutor(new JJKCommand());
        getCommand("spawn").setExecutor(new CompassCommand(this));
        getCommand("jump").setExecutor(new CompassCommand(this));
        getCommand("setspawn").setExecutor(new SetCompassCommand(this));
        getCommand("setjump").setExecutor(new SetCompassCommand(this));
        getCommand("gameinfo").setExecutor(new GameCommand());
        getCommand("jjkstart").setExecutor(new GameStartCommand(this, scoreboardManager));

        // Enregistrement des événements
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ConfigMenu(), this);
        getServer().getPluginManager().registerEvents(new CompassManager(this), this);
        getServer().getPluginManager().registerEvents(new CompassGUI(this), this);
        getServer().getPluginManager().registerEvents(timerConfigMenu, this);
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
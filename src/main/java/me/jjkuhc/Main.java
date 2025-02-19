package me.jjkuhc;

import me.jjkuhc.jjkcompass.CompassCommand;
import me.jjkuhc.jjkcompass.CompassGUI;
import me.jjkuhc.jjkcompass.CompassManager;
import me.jjkuhc.jjkcompass.SetCompassCommand;
import me.jjkuhc.jjkconfig.BorderConfigMenu;
import me.jjkuhc.jjkconfig.ConfigMenu;
import me.jjkuhc.jjkconfig.StartGameMenu;
import me.jjkuhc.jjkgame.GameCommand;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameStartCommand;
import me.jjkuhc.jjkgame.GameState;
import me.jjkuhc.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import me.jjkuhc.jjkconfig.TimerConfigMenu;

public class Main extends JavaPlugin implements Listener {

    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        getLogger().info("JJK UHC Plugin activé !");
        saveDefaultConfig(); // Pour créer le fichier config.yml s'il n'existe pas encore

        // Charger les timers depuis la configuration
        int pvpTimer = getConfig().getInt("timers.pvp", 300); // 600s = 5 min par défaut
        int invincibilityTimer = getConfig().getInt("timers.invincibility", 90); // 120s = 1min30 par défaut

        scoreboardManager = new ScoreboardManager(this);

        getServer().getPluginManager().registerEvents(new BorderConfigMenu(), this);
        getServer().getPluginManager().registerEvents(new StartGameMenu(), this);
        getServer().getPluginManager().registerEvents(new TimerConfigMenu(), this);


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
        getConfig().set("timers.pvp", TimerConfigMenu.getPvpTimer());
        getConfig().set("timers.invincibility", TimerConfigMenu.getInvincibilityTimer());
        saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scoreboardManager.setScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            Bukkit.getLogger().info("[DEBUG] " + player.getName() + " a reçu " + event.getDamage() + " dégâts.");

            if (GameManager.getCurrentState() == GameState.EN_COURS) {
                Bukkit.getLogger().info("[DEBUG] Les dégâts sont activés pour " + player.getName());
                event.setCancelled(false);
            } else {
                Bukkit.getLogger().info("[DEBUG] Dégâts annulés car la partie n'est pas en cours.");
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damager = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;

            Bukkit.getLogger().info("[DEBUG] " + player.getName() + " a été frappé par " + (damager != null ? damager.getName() : "une entité") + " pour " + event.getDamage() + " dégâts.");

            if (GameManager.getCurrentState() == GameState.EN_COURS) {
                Bukkit.getLogger().info("[DEBUG] Dégâts entre joueurs autorisés.");
                event.setCancelled(false);
            } else {
                Bukkit.getLogger().info("[DEBUG] Dégâts annulés car la partie n'est pas en cours.");
            }
        }
    }
}
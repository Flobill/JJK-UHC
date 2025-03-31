package me.jjkuhc;

import me.jjkuhc.jjkcompass.CompassCommand;
import me.jjkuhc.jjkcompass.CompassGUI;
import me.jjkuhc.jjkcompass.CompassManager;
import me.jjkuhc.jjkcompass.SetCompassCommand;
import me.jjkuhc.jjkconfig.*;
import me.jjkuhc.jjkgame.*;
import me.jjkuhc.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener {

    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        getLogger().info("JJK UHC Plugin activé !");
        saveDefaultConfig(); // Pour créer le fichier config.yml s'il n'existe pas encore

        scoreboardManager = new ScoreboardManager(this);
        EnergyManager.startPassiveRegen(this);

        //Les menus
        getServer().getPluginManager().registerEvents(new BorderConfigMenu(), this);
        getServer().getPluginManager().registerEvents(new StartGameMenu(), this);
        getServer().getPluginManager().registerEvents(new TimerConfigMenu(), this);
        getServer().getPluginManager().registerEvents(new SukunaFingerMenu(), this);
        getServer().getPluginManager().registerEvents(new PacteMenu(), this);

        //Les commandes
        getCommand("jjk").setExecutor(new me.jjkuhc.commands.JJKCommand());
        getCommand("spawn").setExecutor(new CompassCommand(this));
        getCommand("jump").setExecutor(new CompassCommand(this));
        getCommand("setspawn").setExecutor(new SetCompassCommand(this));
        getCommand("setjump").setExecutor(new SetCompassCommand(this));
        getServer().getPluginManager().registerEvents(new CompassManager(this), this);
        getServer().getPluginManager().registerEvents(new CompassGUI(this), this);
        getCommand("gameinfo").setExecutor(new GameCommand());
        getCommand("jjkstart").setExecutor(new GameStartCommand(this, scoreboardManager));
        this.getCommand("giveenergy").setExecutor(new EnergyManager());
        getServer().getPluginManager().registerEvents(new DeathManager(), this);

        //Les menus
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ConfigMenu(), this);
        getServer().getPluginManager().registerEvents(new StuffConfigMenu(), this);
        StuffManager.initialize(this);
        getServer().getPluginManager().registerEvents(new RoleConfigMenu(), this);
        getServer().getPluginManager().registerEvents(new CampRoleMenu(), this);
        getServer().getPluginManager().registerEvents(new SukunaFingerListener(), this);

        // ✅ Remet à 0 tous les effets et l'invisibilité
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Supprime tous les effets
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

            // Remet la vie max
            player.setMaxHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());

            // Remet visible Geto
            player.setInvisible(false);

            player.setPlayerListName(player.getName());

            // ✅ Vide l'inventaire
            player.getInventory().clear();

            // ✅ Remet le joueur en mode survie
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        }
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

            // Vérifier si l'invincibilité est active
            if (GameStartCommand.isInvincibilityActive) {
                event.setCancelled(true);
                return;
            }

            // Vérifier si la partie est en cours pour autoriser les dégâts
            if (GameManager.getCurrentState() == GameState.EN_COURS) {
                event.setCancelled(false);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            // Vérifier si le PVP est désactivé
            if (!Bukkit.getWorld("uhc").getPVP()) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
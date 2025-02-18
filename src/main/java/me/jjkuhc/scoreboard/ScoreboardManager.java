package me.jjkuhc.scoreboard;

import me.jjkuhc.host.HostManager;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {

    private final Plugin plugin;

    public ScoreboardManager(Plugin plugin) {
        this.plugin = plugin;
        startUpdatingScoreboard();
    }

    public void setScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("jjkuhc", "dummy", ChatColor.RED + "JJK UHC");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScoreboard(player, scoreboard, objective);
        player.setScoreboard(scoreboard);
    }

    private void updateScoreboard(Player player, Scoreboard scoreboard, Objective objective) {
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        objective.getScore("§7┏━━━━━━━━━━━━━━━━━━┓").setScore(4);
        objective.getScore("§6▪ État : §f" + GameManager.getCurrentState()).setScore(3);
        objective.getScore("§b♟ Joueurs : §a" + Bukkit.getOnlinePlayers().size() + "§7/§c20").setScore(2);
        objective.getScore("§e⭐ Host : §f" + (HostManager.getHost() != null ? HostManager.getHost().getName() : "Aucun")).setScore(1);
        objective.getScore("§7┗━━━━━━━━━━━━━━━━━━┛").setScore(0);
    }

    private void startUpdatingScoreboard() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Scoreboard scoreboard = player.getScoreboard();
                    Objective objective = scoreboard.getObjective("jjkuhc");
                    if (objective != null) {
                        updateScoreboard(player, scoreboard, objective);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Mise à jour toutes les secondes
    }

    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setScoreboard(player);
        }
    }

}
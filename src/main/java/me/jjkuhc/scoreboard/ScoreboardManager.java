package me.jjkuhc.scoreboard;

import me.jjkuhc.host.HostManager;
import me.jjkuhc.jjkgame.EnergyManager;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import me.jjkuhc.jjkroles.RoleType;
import me.jjkuhc.jjkroles.CampType;

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

    public void updateScoreboard(Player player, Scoreboard scoreboard, Objective objective) {
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        RoleType playerRole = GameManager.getPlayerRole(player);
        CampType playerCamp = (playerRole != null) ? playerRole.getCamp() : null;

        objective.getScore("§7┏━━━━━━━━━━━━━━━━━━┓").setScore(8);
        objective.getScore("§6▪ État : §f" + GameManager.getCurrentState().getDisplayName()).setScore(7);
        if (GameManager.isState(GameState.EN_COURS)) {
            int totalSeconds = GameManager.getGameTimeInSeconds();
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;

            String formattedTime = (hours > 0)
                    ? String.format("§f%dh %02dm %02ds", hours, minutes, seconds)
                    : String.format("§f%02dm %02ds", minutes, seconds);

            objective.getScore("§6⏱ Temps : " + formattedTime).setScore(6);
        }
        objective.getScore("§b☺ Joueurs : §a" + Bukkit.getOnlinePlayers().size() + "§7/§c20").setScore(5);
        objective.getScore("§e⭐ Host : §f" + (HostManager.getHost() != null ? HostManager.getHost().getName() : "Aucun")).setScore(4);

        // 🔍 Affichage du rôle et du camp SEULEMENT après la révélation
        if (GameManager.isState(GameState.EN_COURS) && GameManager.areRolesRevealed()) {
            objective.getScore("§d♜ Rôle :").setScore(3);
            objective.getScore("§f" + (playerRole != null ? playerRole.getDisplayName() : "Non attribué")).setScore(3);
            objective.getScore("§d⚔ Camp :").setScore(2);
            objective.getScore("§f" + (playerCamp != null ? playerCamp.getDisplayName() : "Non attribué")).setScore(2);
            int energy = EnergyManager.getEnergy(player);
            objective.getScore("❇ §dÉnergie Occulte :").setScore(1);
            objective.getScore("§b" + energy).setScore(0);
        }

        objective.getScore("§7┗━━━━━━━━━━━━━━━━━━┛").setScore(-1);
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
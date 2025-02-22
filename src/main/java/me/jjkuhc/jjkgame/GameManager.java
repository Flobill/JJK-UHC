package me.jjkuhc.jjkgame;

import me.jjkuhc.jjkconfig.TimerConfigMenu;
import me.jjkuhc.jjkroles.CampManager;
import me.jjkuhc.jjkroles.CampType;
import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.jjkuhc.scoreboard.ScoreboardManager;

import java.util.*;

public class GameManager {
    private static GameState currentState = GameState.EN_ATTENTE;
    private static final Map<UUID, RoleType> playerRoles = new HashMap<>();
    private static boolean rolesRevealed = false;

    public static GameState getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(GameState newState) {
        currentState = newState;
    }

    public static boolean isState(GameState state) {
        return currentState == state;
    }

    public static boolean areRolesRevealed() {
        return rolesRevealed;
    }

    public static void assignRoles() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Set<RoleType> availableRoles = new HashSet<>();
        for (CampType camp : CampType.values()) {
            availableRoles.addAll(CampManager.getInstance().getActiveRoles(camp));
        }
        Collections.shuffle(players);
        List<RoleType> shuffledRoles = new ArrayList<>(availableRoles);
        Collections.shuffle(shuffledRoles);

        int assignedRoles = 0;
        for (Player player : players) {
            if (assignedRoles < availableRoles.size()) {
                RoleType role = shuffledRoles.get(assignedRoles);
                playerRoles.put(player.getUniqueId(), role);
                assignedRoles++;
            } else {
                playerRoles.put(player.getUniqueId(), RoleType.EXORCISTE_BASIQUE);
            }
        }

        // ✅ Démarrer le timer d’annonce des rôles
        startRoleAnnouncementTimer();
    }

    private static void startRoleAnnouncementTimer() {
        int roleAnnouncementTime = TimerConfigMenu.getRoleAnnouncementTimer();

        Bukkit.broadcastMessage("§e⌛ Les rôles seront révélés dans §c" + roleAnnouncementTime + " secondes...");

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("§a⚡ Les rôles ont été révélés !");
                rolesRevealed = true;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    RoleType role = playerRoles.get(player.getUniqueId());
                    if (role != null) {
                        player.sendMessage("§aVous êtes : §b" + role.getDisplayName());
                        if (role == RoleType.GOJO) {
                            EnergyManager.setEnergy(player, 1500);
                        } else {
                            EnergyManager.setEnergy(player, 1000);
                        }
                    }
                }
                // ✅ Mise à jour du scoreboard pour tous les joueurs
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ScoreboardManager scoreboardManager = new ScoreboardManager(Bukkit.getPluginManager().getPlugin("JJKUHC"));
                    scoreboardManager.setScoreboard(player);
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), roleAnnouncementTime * 20L);
    }

    public static RoleType getPlayerRole(Player player) {
        return playerRoles.getOrDefault(player.getUniqueId(), RoleType.EXORCISTE_BASIQUE);
    }
}
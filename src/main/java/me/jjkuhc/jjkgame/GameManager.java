package me.jjkuhc.jjkgame;

import me.jjkuhc.jjkconfig.TimerConfigMenu;
import me.jjkuhc.jjkroles.CampManager;
import me.jjkuhc.jjkroles.CampType;
import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {
    private static GameState currentState = GameState.EN_ATTENTE;
    private static final Map<UUID, RoleType> playerRoles = new HashMap<>();

    public static GameState getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(GameState newState) {
        currentState = newState;
    }

    public static boolean isState(GameState state) {
        return currentState == state;
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

        Bukkit.broadcastMessage("§e🎭 Attribution des rôles en cours...");

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
                Bukkit.broadcastMessage("§a📢 Les rôles ont été révélés !");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    RoleType role = playerRoles.get(player.getUniqueId());
                    if (role != null) {
                        player.sendMessage("§a🎭 Vous êtes : §b" + role.getDisplayName());
                    }
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), roleAnnouncementTime * 20L);
    }

    public static RoleType getPlayerRole(Player player) {
        return playerRoles.getOrDefault(player.getUniqueId(), RoleType.EXORCISTE_BASIQUE);
    }
}
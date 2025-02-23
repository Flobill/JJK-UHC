package me.jjkuhc.jjkgame;

import me.jjkuhc.jjkconfig.SukunaFingerMenu;
import me.jjkuhc.jjkconfig.TimerConfigMenu;
import me.jjkuhc.jjkroles.CampManager;
import me.jjkuhc.jjkroles.CampType;
import me.jjkuhc.jjkroles.RoleType;
import me.jjkuhc.jjkroles.exorcistes.Gojo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.jjkuhc.scoreboard.ScoreboardManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import me.jjkuhc.jjkconfig.SukunaFingerMenu;

import java.util.*;

public class GameManager {
    private static GameState currentState = GameState.EN_ATTENTE;
    private static final Map<UUID, RoleType> playerRoles = new HashMap<>();
    private static boolean rolesRevealed = false;

    // ✅ Variable pour gérer le nombre de doigts à distribuer
    private static int sukunaFingersToDistribute = 2; // Valeur par défaut

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

        // ✅ Attribuer les rôles aux joueurs
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
        // ✅ Distribuer les doigts de Sukuna après 15 secondes
        new BukkitRunnable() {
            @Override
            public void run() {
                distributeSukunaFingers(SukunaFingerMenu.getNumberOfSukunaFingers());
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 300L); // 15 secondes (300 ticks)


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

                        // ✅ Appliquer les effets de Gojo dès l'attribution du rôle
                        if (role == RoleType.GOJO) {
                            Gojo gojo = new Gojo(player);
                            Bukkit.getServer().getPluginManager().registerEvents(gojo, Bukkit.getPluginManager().getPlugin("JJKUHC"));
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

    private static void distributeSukunaFingers(int numberOfFingers) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);

        for (int i = 0; i < numberOfFingers && i < players.size(); i++) {
            Player player = players.get(i);
            ItemStack sukunaFinger = new ItemStack(Material.NETHER_WART);
            player.getInventory().addItem(sukunaFinger);
            player.sendMessage("§5⚡ Vous avez reçu un doigt de Sukuna !");
        }
    }
}
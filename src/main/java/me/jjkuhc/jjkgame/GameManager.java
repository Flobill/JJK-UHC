package me.jjkuhc.jjkgame;

import me.jjkuhc.jjkconfig.PacteMenu;
import me.jjkuhc.jjkconfig.SukunaFingerMenu;
import me.jjkuhc.jjkconfig.TimerConfigMenu;
import me.jjkuhc.jjkroles.CampManager;
import me.jjkuhc.jjkroles.CampType;
import me.jjkuhc.jjkroles.RoleType;
import me.jjkuhc.jjkroles.exorcistes.Gojo;
import me.jjkuhc.jjkroles.exorcistes.Itadori;
import me.jjkuhc.jjkroles.exorcistes.Megumi;
import me.jjkuhc.jjkroles.neutres.Sukuna;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
                        if (role == RoleType.SUKUNA) {
                            Sukuna sukuna = new Sukuna(player, Bukkit.getPluginManager().getPlugin("JJKUHC"));
                            Bukkit.getServer().getPluginManager().registerEvents(sukuna, Bukkit.getPluginManager().getPlugin("JJKUHC"));
                        }
                        if (role == RoleType.ITADORI) {
                            Itadori itadori = new Itadori(player);
                            Bukkit.getServer().getPluginManager().registerEvents(itadori, Bukkit.getPluginManager().getPlugin("JJKUHC"));
                        }
                        if (role == RoleType.MEGUMI) {
                            Megumi megumi = new Megumi(player);
                            Bukkit.getServer().getPluginManager().registerEvents(megumi, Bukkit.getPluginManager().getPlugin("JJKUHC"));
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

    // ✅ Appliquer Résistance I la nuit pour Yuji (Pacte Coopératif)
    public static void applyNightResistance(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (GameManager.getPlayerRole(player) == RoleType.ITADORI &&
                        PacteMenu.getPacte(player).equals("Cooperation")) {
                    if (isNight(player.getWorld())) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 0, false, false));
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 600L); // Vérifie toutes les 30s
    }

    // ✅ Détecter si c'est la nuit
    private static boolean isNight(World world) {
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }

    public static Player getSukunaPlayer() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (getPlayerRole(onlinePlayer) == RoleType.SUKUNA) {
                return onlinePlayer;
            }
        }
        return null; // Retourne null si Sukuna est mort ou n'existe pas
    }

    public static void setFingersToSukuna(Player player) {
        Player sukuna = getSukunaPlayer();
        if (sukuna == null) {
            player.sendMessage("§cErreur : Sukuna n'existe pas ou est mort.");
            return;
        }

        // Vérifie si Itadori a des doigts
        List<ItemStack> fingersToGive = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_WART) {
                fingersToGive.add(item);
            }
        }

        if (fingersToGive.isEmpty()) {
            player.sendMessage("§cVous ne possédez aucun doigt de Sukuna !");
            return;
        }

        // Transfert des doigts à Sukuna
        for (ItemStack finger : fingersToGive) {
            player.getInventory().remove(finger);
            sukuna.getInventory().addItem(finger);
        }

        player.sendMessage("§5⚡ Tous vos doigts ont été donnés à Sukuna !");
        sukuna.sendMessage("§4🔥 Vous avez reçu les doigts d'Itadori !");
    }

    // ✅ Assigner la liste avec Sukuna pour le Pacte d'Ignorance
    public static void assignIgnoranceList(Player yuji) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.remove(yuji); // Enlever Itadori de la liste
        Collections.shuffle(players);

        // ✅ Vérifier qu'il y a suffisamment de joueurs pour éviter les doublons
        List<Player> selected = new ArrayList<>();
        int maxPlayers = Math.min(4, players.size());
        selected.addAll(players.subList(0, maxPlayers));

        Player sukuna = GameManager.getSukunaPlayer();
        if (sukuna != null && !selected.contains(sukuna)) {
            selected.add(sukuna); // Ajoute Sukuna une seule fois
        }

        yuji.sendMessage("§6📜 Joueurs suspectés d'être Sukuna :");
        for (Player p : selected) {
            yuji.sendMessage(" - §b" + p.getName());
        }
    }

    public static void handleEpisodeStart() {
        Bukkit.broadcastMessage("§e🌟 Début d'un nouvel épisode !");

        for (Player player : Bukkit.getOnlinePlayers()) {
            RoleType role = GameManager.getPlayerRole(player);

            // ✅ Vérifie si le joueur est Itadori et applique son passif
            if (role == RoleType.ITADORI) {
                Itadori.checkAndApplyRegeneration(player);
            }
        }
    }

}
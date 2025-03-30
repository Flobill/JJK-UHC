package me.jjkuhc.jjkgame;

import me.jjkuhc.jjkconfig.PacteMenu;
import me.jjkuhc.jjkroles.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class VictoryManager {

    // ✅ Méthode principale à appeler pour vérifier la condition de victoire
    public static void checkVictory() {
        Map<CampType, List<Player>> aliveByCamp = new HashMap<>();
        List<Player> alivePlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() != org.bukkit.GameMode.SURVIVAL) continue;

            RoleType role = GameManager.getPlayerRole(player);
            CampType camp = CampManager.getInstance().getCampOfRole(role);

            // Cas particulier : Itadori & Sukuna en pacte Coopération
            if (role == RoleType.ITADORI && PacteMenu.getPacte(player).equals("Cooperation")) {
                Player sukuna = GameManager.getSukunaPlayer();
                if (sukuna != null && sukuna.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                    // On va attendre de traiter tout le monde pour vérifier si eux deux sont les seuls survivants
                    alivePlayers.add(player); // On ajoute temporairement Itadori
                    alivePlayers.add(sukuna); // Et Sukuna
                    continue;
                }
            }
            // 🧠 Cas spécial : victoire d’Itadori + Sukuna s’ils sont les deux seuls survivants
            Player sukuna = GameManager.getSukunaPlayer();
            Player itadori = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> GameManager.getPlayerRole(p) == RoleType.ITADORI)
                    .findFirst().orElse(null);

            if (itadori != null && sukuna != null
                    && PacteMenu.getPacte(itadori).equals("Cooperation")
                    && itadori.getGameMode() == org.bukkit.GameMode.SURVIVAL
                    && sukuna.getGameMode() == org.bukkit.GameMode.SURVIVAL
                    && Bukkit.getOnlinePlayers().stream().filter(p -> p.getGameMode() == org.bukkit.GameMode.SURVIVAL).count() == 2) {

                Bukkit.broadcastMessage("§f[§9JJK UHC§f] §dVictoire de §lItadori & Sukuna§d grâce au Pacte Coopératif !");
                itadori.sendTitle("§dVictoire Duo !", "§fAvec Sukuna grâce au Pacte", 10, 100, 20);
                sukuna.sendTitle("§dVictoire Duo !", "§fAvec Itadori grâce au Pacte", 10, 100, 20);
                return;
            }


            aliveByCamp.computeIfAbsent(camp, k -> new ArrayList<>()).add(player);
            alivePlayers.add(player);
        }

        if (aliveByCamp.size() == 1) {
            CampType winner = aliveByCamp.keySet().iterator().next();
            Bukkit.broadcastMessage("§f[§9JJK UHC§f] §aVictoire du camp §l" + winner.getDisplayName() + "§a !");
            for (Player p : alivePlayers) {
                p.sendTitle("§6Victoire !", "§fCamp gagnant : §e" + winner.getDisplayName(), 10, 100, 20);
            }
        }
    }
}
package me.jjkuhc.jjkgame;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import me.jjkuhc.jjkgame.VictoryManager;

public class DeathManager implements Listener {

    private static boolean deathMessageEnabled = true;

    public static boolean isDeathMessageEnabled() {
        return deathMessageEnabled;
    }

    public static void toggleDeathMessage() {
        deathMessageEnabled = !deathMessageEnabled;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!deathMessageEnabled) return;

        Player deceased = event.getEntity();

        // 🚫 Supprimer les Nether Stars des drops
        event.getDrops().removeIf(item -> item != null && item.getType() == Material.NETHER_STAR);

        // 🔇 Supprime le message par défaut
        event.setDeathMessage(null);

        // 🔔 Affiche le message stylé
        String role = GameManager.getPlayerRole(deceased).getDisplayName();
        String message = "§c" + deceased.getName() + " §7est mort, son rôle était §c" + role;

        Bukkit.broadcastMessage("§8§m==========================================");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§8[§c☠§8] §l" + message);
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§8§m==========================================");

        // 💥 Son d'éclair
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }


        // ⏩ TP au setworldspawn
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
            deceased.teleport(Bukkit.getWorld("uhc").getSpawnLocation());
            deceased.spigot().respawn();
        }, 2L); // Juste après la mort

        // 🕒 Après 10 secondes → passage en spectateur
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
            deceased.setGameMode(org.bukkit.GameMode.SPECTATOR);
            deceased.sendMessage("§7☠ Vous êtes maintenant spectateur.");
        }, 200L); // 10 sec = 200 ticks

        // 🧠 Vérifie la victoire après 11 secondes
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
            VictoryManager.checkVictory();
        }, 220L);
    }

    public static void broadcastDeathMessage(Player deceased) {
        String role = GameManager.getPlayerRole(deceased).getDisplayName();
        String message = "§c" + deceased.getName() + " §7est mort, son rôle était §c" + role;

        Bukkit.broadcastMessage("§8§m==========================================");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§8[§c☠§8] §l" + message);
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§8§m==========================================");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
    }

}

package me.jjkuhc.jjkgame;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class SukunaFingerListener implements Listener {

    // ✅ Vérifie si le joueur a encore des doigts de Sukuna
    private void updateSukunaHearts(Player player) {
        int fingerCount = 0;

        // ✅ Compter les doigts dans l'inventaire
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_WART) {
                fingerCount += item.getAmount();
            }
        }

        // ✅ Mise à jour des cœurs supplémentaires
        double baseHealth = 20.0; // 10 cœurs de base
        double extraHealth = baseHealth + (fingerCount * 2); // Chaque doigt donne 1 cœur = 2 points de vie

        if (player.getMaxHealth() != extraHealth) {
            player.setMaxHealth(extraHealth);
        }
    }

    // ✅ Met à jour lorsque l'inventaire est fermé
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            updateSukunaHearts((Player) event.getPlayer());
        }
    }

    // ✅ Met à jour lorsque le joueur clique dans son inventaire
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
            updateSukunaHearts(player);
        }, 1L);
    }

    // ✅ Vérifie lors du ramassage d'un objet
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getItemStack().getType() == Material.NETHER_WART) {
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
                updateSukunaHearts(player);
            }, 1L);
        }
    }

    // ✅ Vérifie lors du drop d'un objet
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
            updateSukunaHearts(player);
        }, 1L);
    }

    // ✅ Mise à jour des cœurs quand un joueur rejoint
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateSukunaHearts(event.getPlayer());
    }
}
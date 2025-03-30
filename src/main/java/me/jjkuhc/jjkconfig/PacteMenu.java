package me.jjkuhc.jjkconfig;

import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class PacteMenu implements Listener {
    private static final HashMap<UUID, String> playerPacts = new HashMap<>(); // Stocke les pactes des joueurs

    public static void openPacteMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§c⚖ Choix du Pacte");

        // ❌ Pacte d'Ignorance
        ItemStack ignorance = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta ignoranceMeta = ignorance.getItemMeta();
        ignoranceMeta.setDisplayName("§c❌ Pacte d'Ignorance");
        ignoranceMeta.setLore(Arrays.asList(
                "§7→ Vous restez du côté des exorcistes.",
                "§7→ Vous connaîtrez l’identité de Sukuna.",
                "§7→ Vous infligez §c+10% de dégâts§7 à Sukuna."
        ));
        ignorance.setItemMeta(ignoranceMeta);
        inv.setItem(11, ignorance);

        // ✅ Pacte Coopératif
        ItemStack cooperation = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta cooperationMeta = cooperation.getItemMeta();
        cooperationMeta.setDisplayName("§a✅ Pacte Coopératif");
        cooperationMeta.setLore(Arrays.asList(
                "§7→ Vous rejoignez Sukuna.",
                "§7→ Vous obtenez §6Résistance I§7 la nuit.",
                "§7→ Tous vos doigts sont donnés à Sukuna."
        ));
        cooperation.setItemMeta(cooperationMeta);
        inv.setItem(15, cooperation);

        // 📖 Explication au centre
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.setDisplayName("§6📖 Choisissez votre Pacte !");
        bookMeta.setLore(Arrays.asList(
                "§7Vous devez faire un choix crucial...",
                "§7Sélectionnez le pacte qui définira",
                "§7votre destin dans cette partie !"
        ));
        book.setItemMeta(bookMeta);
        inv.setItem(13, book);

        // 🔙 Flèche de retour
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§a🔙 Retour");
        back.setItemMeta(backMeta);
        inv.setItem(26, back);

        player.openInventory(inv);
    }

    // 📌 Gestion des clics dans le menu
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§c⚖ Choix du Pacte")) return;
        event.setCancelled(true); // Empêche les déplacements d'items
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        if (itemName.equals("§c❌ Pacte d'Ignorance")) {
            selectIgnorancePact(player);
        } else if (itemName.equals("§a✅ Pacte Coopératif")) {
            selectCooperationPact(player);
        } else if (itemName.equals("§a🔙 Retour")) {
            player.closeInventory();
        }
    }

    // 📌 Pacte d'Ignorance
    private void selectIgnorancePact(Player player) {
        playerPacts.put(player.getUniqueId(), "Ignorance");
        player.sendMessage("§c⚖ Vous avez choisi le Pacte d'Ignorance !");

        // ✅ Ajoute la liste des 5 joueurs (avec Sukuna inclus)
        GameManager.assignIgnoranceList(player);

        // ✅ Ajoute +10% de dégâts contre Sukuna (plus tard dans les combats)
        // TODO: Implémenter les dégâts augmentés

        player.closeInventory();
    }

    // 📌 Pacte Coopératif
    private void selectCooperationPact(Player player) {
        playerPacts.put(player.getUniqueId(), "Cooperation");
        player.sendMessage("§aVous avez choisi le Pacte Coopératif !");
        Player sukuna = GameManager.getSukunaPlayer();
        if (sukuna != null && sukuna.isOnline()) {
            sukuna.sendMessage("§f[§9JJK UHC§f] §l§6Itadori a choisi le Pacte Coopératif, vous devez désormais gagner ensemble§6 !");
            sukuna.playSound(sukuna.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        }

        // ✅ Ajoute Résistance I la nuit
        player.sendMessage("§7Vous obtenez §6Résistance I la nuit !");
        GameManager.applyNightResistance(player);

        // ✅ Envoie automatiquement les doigts à Sukuna
        GameManager.setFingersToSukuna(player);

        player.closeInventory();
    }

    // 📌 Récupérer le pacte d'un joueur
    public static String getPacte(Player player) {
        return playerPacts.getOrDefault(player.getUniqueId(), "Aucun");
    }
}

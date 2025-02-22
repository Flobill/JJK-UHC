package me.jjkuhc.jjkconfig;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import java.util.ArrayList;
import java.util.List;

public class StuffManager implements Listener {
    private static JavaPlugin plugin;

    public static void initialize(JavaPlugin mainPlugin) {
        plugin = mainPlugin;
        plugin.saveDefaultConfig();
        plugin.getServer().getPluginManager().registerEvents(new StuffManager(), plugin); // Enregistre les events
    }

    public static void prepareStuffSetup(Player player) {
        player.getInventory().clear();
        player.sendMessage("§eVotre inventaire est vide, ajoutez les items pour le stuff de départ !");
    }

    public static void saveStuff(Player player) {
        FileConfiguration config = plugin.getConfig();
        List<ItemStack> items = new ArrayList<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
            }
        }

        config.set("stuff", items);
        plugin.saveConfig();
        player.sendMessage("§aStuff enregistré !");
        player.getInventory().clear();
    }

    public static void showSavedStuff(Player player) {
        FileConfiguration config = plugin.getConfig();
        Inventory preview = Bukkit.createInventory(null, 54, "§bStuff Sauvegardé");

        List<?> items = config.getList("stuff");
        if (items != null) {
            for (int i = 0; i < items.size() && i < preview.getSize() - 1; i++) { // -1 pour garder le dernier slot pour la flèche
                if (items.get(i) instanceof ItemStack) {
                    preview.setItem(i, (ItemStack) items.get(i));
                }
            }
        }

        // Ajouter une flèche de retour dans le dernier slot
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta(); // Obtenir le meta de l'item
        if (arrowMeta != null) {
            arrowMeta.setDisplayName("§7Retour"); // Modifier le nom
            arrow.setItemMeta(arrowMeta); // Appliquer les changements
        }
        preview.setItem(53, arrow);

        player.openInventory(preview);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§bStuff Sauvegardé")) return;

        event.setCancelled(true); // Empêche la modification de l'inventaire

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Vérifier si le joueur clique sur la flèche de retour
        if (clickedItem.getType() == Material.ARROW && clickedItem.getItemMeta().getDisplayName().equals("§7Retour")) {
            new StuffConfigMenu().open(player);
        }
    }

    public static void giveStuff(Player player) {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("stuff")) {
            player.sendMessage("§c⚠ Aucun stuff de départ enregistré !");
            return;
        }

        player.getInventory().clear(); // Nettoie l'inventaire

        List<?> itemList = config.getList("stuff");
        if (itemList == null) return;

        for (Object obj : itemList) {
            if (obj instanceof ItemStack) {
                player.getInventory().addItem((ItemStack) obj);
            }
        }
    }
}
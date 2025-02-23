package me.jjkuhc.jjkconfig;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SukunaFingerMenu implements Listener {

    private static int numberOfSukunaFingers = 2; // Valeur par défaut

    // ✅ Ouvre le menu des doigts de Sukuna
    public static void openFingerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§5Gestion des Doigts de Sukuna");

        // ✅ Remplir le menu avec des vitres pour le design
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // ✅ Doigt de Sukuna au centre
        ItemStack sukunaFinger = new ItemStack(Material.NETHER_WART);
        ItemMeta fingerMeta = sukunaFinger.getItemMeta();
        fingerMeta.setDisplayName("§5Doigt de Sukuna");
        fingerMeta.setLore(java.util.Arrays.asList("§7Nombre de doigts à distribuer :", "§b" + numberOfSukunaFingers));
        sukunaFinger.setItemMeta(fingerMeta);
        inv.setItem(13, sukunaFinger); // Position centrale

        // ✅ Flèche de retour
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        arrowMeta.setDisplayName("§aRetour");
        arrow.setItemMeta(arrowMeta);
        inv.setItem(22, arrow);

        // ✅ Ouvrir l'inventaire
        player.openInventory(inv);
    }

    // ✅ Gérer les clics dans le menu
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§5Gestion des Doigts de Sukuna")) return;

        event.setCancelled(true); // Empêche de déplacer les items
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        // ✅ Gérer les clics sur le doigt de Sukuna
        if (itemName.equals("§5Doigt de Sukuna")) {
            if (event.isLeftClick()) {
                numberOfSukunaFingers++;
                player.sendMessage("§a+1 doigt ajouté. Nombre actuel : " + numberOfSukunaFingers);
            } else if (event.isRightClick() && numberOfSukunaFingers > 0) {
                numberOfSukunaFingers--;
                player.sendMessage("§c-1 doigt retiré. Nombre actuel : " + numberOfSukunaFingers);
            }
            openFingerMenu(player); // Met à jour l'interface
        }

        // ✅ Retour au menu principal
        if (itemName.equals("§aRetour")) {
            new ConfigMenu().open(player);
        }
    }

    // ✅ Obtenir le nombre de doigts à distribuer
    public static int getNumberOfSukunaFingers() {
        return numberOfSukunaFingers;
    }
}
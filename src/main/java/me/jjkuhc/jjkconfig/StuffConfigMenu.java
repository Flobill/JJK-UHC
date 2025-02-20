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

public class StuffConfigMenu implements Listener {
    private static final String TITLE = "§eConfiguration du Stuff";

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Remplissage avec des vitres grises
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Coffre (choisir son stuff)
        setItem(inv, 11, Material.CHEST, "§6Configurer le Stuff");

        // Wagon avec coffre (voir le stuff sauvegardé)
        setItem(inv, 13, Material.CHEST_MINECART, "§bVoir le Stuff Sauvegardé");

        // Barrière (valider le stuff)
        setItem(inv, 15, Material.BARRIER, "§cValider le Stuff");

        // Ajouter une flèche de retour dans le dernier slot
        setItem(inv, 26, Material.ARROW, "§7Retour");

        player.openInventory(inv);
    }

    private void setItem(Inventory inv, int slot, Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        switch (itemName) {
            case "§6Configurer le Stuff":
                StuffManager.prepareStuffSetup(player);
                break;
            case "§bVoir le Stuff Sauvegardé":
                StuffManager.showSavedStuff(player);
                break;
            case "§cValider le Stuff":
                StuffManager.saveStuff(player);
                break;
            case "§7Retour":
                new ConfigMenu().open(player);
                return;
        }
    }
}
package me.jjkuhc.jjkconfig;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class ConfigMenu implements Listener {

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§7Options §bJJK");

        // Remplir les cases vides avec des vitres rouges
        ItemStack redGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redGlassMeta = redGlass.getItemMeta();
        redGlassMeta.setDisplayName(" ");
        redGlass.setItemMeta(redGlassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, redGlass);
        }

        // Ajout des items du menu
        setItem(inv, 10, Material.LIME_WOOL, "§aLancer la Partie");
        setItem(inv, 12, Material.BEACON, "§bConfiguration Rôles");
        setItem(inv, 14, Material.CLOCK, "§eConfiguration Timers");
        setItem(inv, 16, Material.CHEST, "§6Configuration Stuff");
        setItem(inv, 22, Material.GLASS, "§fConfiguration Bordure");

        player.openInventory(inv);
    }

    private void setItem(Inventory inv, int slot, Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    // Empêcher le déplacement d'items dans le menu
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§7Options §bJJK")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked(); // Définir le joueur
            ItemStack clickedItem = event.getCurrentItem(); // Définir l'item cliqué

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            String itemName = clickedItem.getItemMeta().getDisplayName(); // Définir le nom de l'item

            if (clickedItem.getType() == Material.GLASS && itemName.equals("§fConfiguration Bordure")) {
                new BorderConfigMenu().open(player);
            }
        }
    }
}
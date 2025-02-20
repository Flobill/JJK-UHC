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

public class RoleConfigMenu implements Listener {
    private static final String MENU_TITLE = "§eConfiguration Rôles";

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        // Ajout des vitres rouges par défaut (exorcistes, fléaux, neutres)
        setCampItem(inv, 46, Material.RED_STAINED_GLASS_PANE, "§cExorcistes");
        setCampItem(inv, 49, Material.RED_STAINED_GLASS_PANE, "§cFléaux");
        setCampItem(inv, 52, Material.RED_STAINED_GLASS_PANE, "§cNeutres");

        // Flèche de retour
        setItem(inv, 53, Material.ARROW, "§7Retour");

        player.openInventory(inv);
    }

    private void setCampItem(Inventory inv, int slot, Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
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
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        switch (itemName) {
            case "§cExorcistes":
                new CampRoleMenu().open(player, "Exorcistes");
                break;
            case "§cFléaux":
                new CampRoleMenu().open(player, "Fléaux");
                break;
            case "§cNeutres":
                new CampRoleMenu().open(player, "Neutres");
                break;
            case "§7Retour":
                new ConfigMenu().open(player);
                break;
        }
    }
}
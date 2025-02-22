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

public class CampRoleMenu implements Listener {
    private static final String MENU_TITLE = "§eRôles : ";

    public void open(Player player, String camp) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE + camp);

        // Placeholder pour les rôles (on ajoutera les rôles plus tard)
        for (int i = 0; i < 18; i++) {
            inv.setItem(i, new ItemStack(Material.BARRIER)); // À remplacer par les rôles
        }

        // Ajout des vitres de navigation entre camps
        setCampItem(inv, 45, Material.RED_STAINED_GLASS_PANE, "§cExorcistes", camp.equals("Exorcistes"));
        setCampItem(inv, 47, Material.RED_STAINED_GLASS_PANE, "§cFléaux", camp.equals("Fléaux"));
        setCampItem(inv, 49, Material.RED_STAINED_GLASS_PANE, "§cYuta & Rika", camp.equals("Yuta & Rika"));
        setCampItem(inv, 51, Material.RED_STAINED_GLASS_PANE, "§cNeutres", camp.equals("Neutres"));

        // Flèche de retour
        setItem(inv, 53, Material.ARROW, "§7Retour");

        player.openInventory(inv);
    }

    private void setCampItem(Inventory inv, int slot, Material material, String name, boolean selected) {
        ItemStack item = new ItemStack(selected ? Material.GREEN_STAINED_GLASS_PANE : material);
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
        if (!event.getView().getTitle().startsWith(MENU_TITLE)) return;
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
            case "§cYuta & Rika":
                new CampRoleMenu().open(player, "Yuta & Rika");
                break;
            case "§cNeutres":
                new CampRoleMenu().open(player, "Neutres");
                break;
            case "§7Retour":
                new RoleConfigMenu().open(player);
                break;
        }
    }
}
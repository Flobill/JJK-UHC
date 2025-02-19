package me.jjkuhc.jjkconfig;

import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BorderConfigMenu implements Listener {

    private final String MENU_TITLE = "§6Configuration de la Bordure";
    private final int MIN_BORDER_SIZE = 100;
    private final int MAX_BORDER_SIZE = 5000;

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Configuration de la Bordure");

        // Remplir tout l'inventaire avec des vitres grises
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Boutons pour ajuster la bordure
        setItem(inv, 11, Material.STONE_BUTTON, "§c-100");
        setItem(inv, 12, Material.STONE_BUTTON, "§c-10");

        // Bloc de verre pour afficher la taille actuelle de la bordure
        int borderSize = (int) Bukkit.getWorld("uhc").getWorldBorder().getSize();
        setItem(inv, 13, Material.GLASS, "§fBordure actuelle : §e" + borderSize);

        // Boutons pour augmenter la bordure
        setItem(inv, 14, Material.STONE_BUTTON, "§a+10");
        setItem(inv, 15, Material.STONE_BUTTON, "§a+100");

        // Flèche de retour
        setItem(inv, 26, Material.ARROW, "§fRetour");

        player.openInventory(inv);
    }

    private void setItem(Inventory inv, int slot, Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private void updateBorderDisplay(Inventory inv) {
        World uhcWorld = Bukkit.getWorld("uhc");
        if (uhcWorld != null) {
            double borderSize = uhcWorld.getWorldBorder().getSize();
            ItemStack borderDisplay = new ItemStack(Material.GLASS);
            ItemMeta meta = borderDisplay.getItemMeta();
            meta.setDisplayName("§b🌐 Bordure actuelle : §a" + (int) borderSize + " §bblocs");
            borderDisplay.setItemMeta(meta);
            inv.setItem(4, borderDisplay);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Configuration de la Bordure")) return;

        event.setCancelled(true); // Empêche le déplacement d'items

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();
        int borderSize = (int) Bukkit.getWorld("uhc").getWorldBorder().getSize();

        // Gérer les clics sur les boutons de la bordure
        switch (itemName) {
            case "§c-100":
                borderSize = Math.max(10, borderSize - 100);
                break;
            case "§c-10":
                borderSize = Math.max(10, borderSize - 10);
                break;
            case "§a+10":
                borderSize = Math.min(10000, borderSize + 10);
                break;
            case "§a+100":
                borderSize = Math.min(10000, borderSize + 100);
                break;
            case "§fRetour":
                new ConfigMenu().open(player);
                return;
            default:
                return;
        }

        // Mettre à jour la bordure du monde "uhc"
        Bukkit.getWorld("uhc").getWorldBorder().setSize(borderSize);
        player.sendMessage("§aNouvelle bordure définie à : " + borderSize + " blocs.");

        // Rouvre le menu pour mettre à jour l'affichage
        new BorderConfigMenu().open(player);
    }
}
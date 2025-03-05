package me.jjkuhc.jjkconfig;

import me.jjkuhc.jjkroles.CampManager;
import me.jjkuhc.jjkroles.CampType;
import me.jjkuhc.jjkroles.RoleType;
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
    private static final String MENU_TITLE = "§eConfiguration des Rôles";

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);
        CampManager campManager = CampManager.getInstance();
        CampType currentCamp = campManager.getCurrentCamp();

        // Ajout des rôles du camp sélectionné
        int slot = 10;
        for (RoleType role : RoleType.values()) {
            // Vérifier si le rôle appartient au camp actuel
            if (getCampForRole(role) == currentCamp) {
                boolean isActive = campManager.getActiveRoles(currentCamp).contains(role);
                Material material = isActive ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
                setItem(inv, slot, material, "§b" + role.name());

                slot++;
                if (slot == 17 || slot == 26) { // Passer à la ligne suivante
                    slot += 2;
                }
            }
        }

        // Ajout des vitres pour les camps
        setCampItem(inv, 45, Material.RED_STAINED_GLASS_PANE, "§cExorcistes", CampType.EXORCISTES);
        setCampItem(inv, 47, Material.RED_STAINED_GLASS_PANE, "§cFléaux", CampType.FLEAUX);
        setCampItem(inv, 49, Material.RED_STAINED_GLASS_PANE, "§cYuta & Rika", CampType.YUTA_RIKA);
        setCampItem(inv, 51, Material.RED_STAINED_GLASS_PANE, "§cNeutres", CampType.NEUTRES);

        // Marquer le camp actif en vert
        markActiveCamp(inv, currentCamp);

        // Flèche de retour
        setItem(inv, 53, Material.ARROW, "§7Retour");

        player.openInventory(inv);
    }

    private void setItem(Inventory inv, int slot, Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private void setCampItem(Inventory inv, int slot, Material material, String name, CampType camp) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private void markActiveCamp(Inventory inv, CampType activeCamp) {
        int slot;
        switch (activeCamp) {
            case EXORCISTES -> slot = 45;
            case FLEAUX -> slot = 47;
            case YUTA_RIKA -> slot = 49;
            case NEUTRES -> slot = 51;
            default -> { return; }
        }
        ItemStack activeGlass = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = activeGlass.getItemMeta();
        meta.setDisplayName("§a" + activeCamp.getDisplayName());
        activeGlass.setItemMeta(meta);
        inv.setItem(slot, activeGlass);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        CampManager campManager = CampManager.getInstance();
        String itemName = clickedItem.getItemMeta().getDisplayName();

        // Gestion du changement de camp
        for (CampType camp : CampType.values()) {
            if (itemName.equals("§c" + camp.getDisplayName()) || itemName.equals("§a" + camp.getDisplayName())) {
                campManager.setCurrentCamp(camp);
                open(player);
                return;
            }
        }

        // Gestion des rôles (Activation/Désactivation)
        for (RoleType role : RoleType.values()) {
            if (itemName.equals("§b" + role.name())) {
                campManager.toggleRole(campManager.getCurrentCamp(), role);
                open(player);
                return;
            }
        }

        // Flèche de retour
        if (itemName.equals("§7Retour")) {
            new ConfigMenu().open(player);
        }
    }

    private CampType getCampForRole(RoleType role) {
        return switch (role) {
            case GOJO, ITADORI, MEGUMI, NOBARA -> CampType.EXORCISTES;
            case JOGO, HANAMI -> CampType.FLEAUX;
            case TOJI, SUKUNA -> CampType.NEUTRES;
            case YUTA, RIKA -> CampType.YUTA_RIKA;
            default -> CampType.EXORCISTES;
        };
    }
}
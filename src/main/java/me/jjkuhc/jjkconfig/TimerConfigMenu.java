// Implémentation du système de timers personnalisable pour le PVP et l'invincibilité
// dans le plugin JJK UHC pour Minecraft en Java

// Le menu "TimerConfigMenu.java" permet d'ajuster dynamiquement les timers via un inventaire interactif

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

public class TimerConfigMenu implements Listener {
    private static int pvpTimer = 300; // Par défaut 10 minutes
    private static int invincibilityTimer = 90; // Par défaut 2 minutes
    private String selectedTimer = "PVP"; // PVP sélectionné par défaut

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eConfiguration des Timers");

        // Remplissage avec des vitres grises
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Boutons pour ajuster les timers
        setItem(inv, 10, Material.STONE_BUTTON, "§c-1 min");
        setItem(inv, 11, Material.STONE_BUTTON, "§c-30 s");
        setItem(inv, 12, Material.STONE_BUTTON, "§c-10 s");
        setItem(inv, 14, Material.STONE_BUTTON, "§a+10 s");
        setItem(inv, 15, Material.STONE_BUTTON, "§a+30 s");
        setItem(inv, 16, Material.STONE_BUTTON, "§a+1 min");

        // Affichage dynamique du timer sélectionné
        String timerDisplay = selectedTimer.equals("PVP") ? formatTime(pvpTimer) : formatTime(invincibilityTimer);
        setItem(inv, 13, Material.CLOCK, "§e" + timerDisplay);

        // Sélecteurs de timer (PVP ou Invincibilité)
        setItem(inv, 3, Material.IRON_SWORD, "§bTimer PVP");
        setItem(inv, 5, Material.SHIELD, "§bTimer Invincibilité");

        // Flèche de retour
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

    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else {
            int minutes = seconds / 60;
            int sec = seconds % 60;
            return minutes + "m " + sec + "s";
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§eConfiguration des Timers")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        switch (itemName) {
            case "§c-1 min":
                adjustTimer(-60);
                break;
            case "§c-30 s":
                adjustTimer(-30);
                break;
            case "§c-10 s":
                adjustTimer(-10);
                break;
            case "§a+10 s":
                adjustTimer(10);
                break;
            case "§a+30 s":
                adjustTimer(30);
                break;
            case "§a+1 min":
                adjustTimer(60);
                break;
            case "§bTimer PVP":
                selectedTimer = "PVP";
                break;
            case "§bTimer Invincibilité":
                selectedTimer = "Invincibilité";
                break;
            case "§7Retour":
                new ConfigMenu().open(player);
                return;
        }
        open(player); // Rafraîchir le menu
    }

    private void adjustTimer(int amount) {
        if (selectedTimer.equals("PVP")) {
            pvpTimer = Math.max(10, pvpTimer + amount);
        } else {
            invincibilityTimer = Math.max(10, invincibilityTimer + amount);
        }
    }

    public static int getPvpTimer() {
        return pvpTimer;
    }

    public static int getInvincibilityTimer() {
        return invincibilityTimer;
    }
}
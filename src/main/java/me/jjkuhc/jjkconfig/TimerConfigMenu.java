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
    private static int roleAnnouncementTimer = 120; // Par défaut 2 minutes
    static int dayDuration = 60;   // Par défaut 6 min
    static int nightDuration = 60; // Par défaut 6 min


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
        String timerDisplay;
        if (selectedTimer.equals("PVP")) {
            timerDisplay = formatTime(pvpTimer);
        } else if (selectedTimer.equals("Invincibilité")) {
            timerDisplay = formatTime(invincibilityTimer);
        } else if (selectedTimer.equals("Annonce des rôles")) {
            timerDisplay = formatTime(roleAnnouncementTimer);
        } else if (selectedTimer.equals("Jour")) {
            timerDisplay = formatTime(dayDuration);
        } else if (selectedTimer.equals("Nuit")) {
            timerDisplay = formatTime(nightDuration);
        } else {
            timerDisplay = "0s";
        }
        setItem(inv, 13, Material.CLOCK, "§e" + timerDisplay);


        // Sélecteurs de timer (PVP, Invincibilité, rôles)
        setItem(inv, 3, Material.IRON_SWORD, "§bTimer PVP");
        setItem(inv, 5, Material.SHIELD, "§bTimer Invincibilité");
        setItem(inv, 7, Material.NAME_TAG, "§bAnnonce des rôles");
        setItem(inv, 20, Material.SUNFLOWER, "§eTemps de Jour");
        setItem(inv, 21, Material.BLACK_BED, "§8Temps de Nuit");

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
            case "§bAnnonce des rôles":
                selectedTimer = "Annonce des rôles";
                break;
            case "§7Retour":
                new ConfigMenu().open(player);
                return;
            case "§eTemps de Jour":
                selectedTimer = "Jour";
                break;
            case "§8Temps de Nuit":
                selectedTimer = "Nuit";
                break;
        }
        open(player); // Rafraîchir le menu
    }

    private void adjustTimer(int amount) {
        if (selectedTimer.equals("PVP")) {
            pvpTimer = Math.max(10, pvpTimer + amount);
        } else if (selectedTimer.equals("Invincibilité")) {
            invincibilityTimer = Math.max(10, invincibilityTimer + amount);
        } else if (selectedTimer.equals("Annonce des rôles")) {
            roleAnnouncementTimer = Math.max(5, roleAnnouncementTimer + amount); // Ajout du timer correct
        }
        else if (selectedTimer.equals("Jour")) {
            dayDuration = Math.max(60, dayDuration + amount); // Minimum 1 min
        } else if (selectedTimer.equals("Nuit")) {
            nightDuration = Math.max(60, nightDuration + amount); // Minimum 1 min
        }
    }

    public static int getPvpTimer() {
        return pvpTimer;
    }

    public static int getInvincibilityTimer() {
        return invincibilityTimer;
    }

    public static int getRoleAnnouncementTimer() {
        return roleAnnouncementTimer;
    }

    public static int getDayDuration() {
        return dayDuration;
    }

    public static int getNightDuration() {
        return nightDuration;
    }
}
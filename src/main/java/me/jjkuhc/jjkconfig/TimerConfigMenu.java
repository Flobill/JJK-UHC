package me.jjkuhc.jjkconfig;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class TimerConfigMenu implements Listener {

    private Inventory inventory;
    private String selectedTimer = "PvP"; // Par défaut
    private final Map<String, Integer> timers = new HashMap<>();

    public TimerConfigMenu() {
        timers.put("PvP", 300); // 5 minutes
        timers.put("Invincibilité", 600); // 10 minutes
        timers.put("Annonce des rôles", 120); // 2 minutes
    }

    public void open(Player player) {
        inventory = Bukkit.createInventory(null, 36, "Configuration des Timers");

        // Remplir avec des vitres pour le design
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glassPane);
        }

        // Montres pour sélectionner le timer
        setTimerItem(19, "PvP", player);
        setTimerItem(20, "Invincibilité", player);
        setTimerItem(21, "Annonce des rôles", player);

        // Boutons de gestion du temps
        setButton(10, Material.STONE_BUTTON, "-1 minute");
        setButton(11, Material.STONE_BUTTON, "-30 secondes");
        setButton(12, Material.STONE_BUTTON, "-10 secondes");
        setButton(14, Material.STONE_BUTTON, "+10 secondes");
        setButton(15, Material.STONE_BUTTON, "+30 secondes");
        setButton(16, Material.STONE_BUTTON, "+1 minute");

        // Montre centrale pour afficher le temps sélectionné
        updateCentralClock();

        // Flèche de retour
        setButton(35, Material.ARROW, "Retour");

        player.openInventory(inventory);
    }

    private void ensureInventoryInitialized(Player player) {
        if (this.inventory == null) {
            Bukkit.getLogger().warning("[JJKUHC] Réinitialisation forcée de l'inventaire des timers...");
            open(player); // Réouvre l'inventaire pour éviter le null
        }
    }

    private void updateCentralClock(Player player) {
        ensureInventoryInitialized(player);

        if (this.inventory == null) {
            Bukkit.getLogger().severe("[JJKUHC] Impossible de mettre à jour l'horloge centrale, inventaire toujours null !");
            return;
        }

        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();
        int time = timers.get(selectedTimer);

        String timeDisplay;
        if (time >= 60) {
            int minutes = time / 60;
            int seconds = time % 60;
            if (seconds > 0) {
                timeDisplay = minutes + " min " + seconds + " sec";
            } else {
                timeDisplay = minutes + " min";
            }
        } else {
            timeDisplay = time + " sec";
        }

        meta.setDisplayName(ChatColor.AQUA + selectedTimer + " : " + ChatColor.YELLOW + timeDisplay);
        clock.setItemMeta(meta);
        inventory.setItem(13, clock);
    }

    private void setTimerItem(int slot, String name, Player player) {
        ensureInventoryInitialized(player);

        if (this.inventory == null) {
            Bukkit.getLogger().severe("[JJKUHC] Impossible de définir l'élément de la montre, inventaire toujours null !");
            return;
        }

        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();
        meta.setDisplayName((selectedTimer.equals(name) ? ChatColor.GREEN : ChatColor.YELLOW) + name);
        clock.setItemMeta(meta);
        inventory.setItem(slot, clock);
    }

    private void setButton(int slot, Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + name);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private void updateCentralClock() {
        if (this.inventory == null) {
            Bukkit.getLogger().warning("[JJKUHC] L'inventaire de configuration des timers est null ! Réinitialisation...");
            return;
        }

        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();
        int time = timers.get(selectedTimer);

        String timeDisplay = time >= 60 ? (time / 60) + " min" : time + " sec";
        meta.setDisplayName(ChatColor.AQUA + selectedTimer + " : " + ChatColor.YELLOW + timeDisplay);
        clock.setItemMeta(meta);
        inventory.setItem(13, clock);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Configuration des Timers")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        ensureInventoryInitialized(player); // 🛡️ Ajout de cette ligne de sécurité

        switch (itemName) {
            case "Retour":
                new ConfigMenu().open(player);
                break;
            case "-1 minute":
                modifyTimer(-60);
                break;
            case "-30 secondes":
                modifyTimer(-30);
                break;
            case "-10 secondes":
                modifyTimer(-10);
                break;
            case "+10 secondes":
                modifyTimer(10);
                break;
            case "+30 secondes":
                modifyTimer(30);
                break;
            case "+1 minute":
                modifyTimer(60);
                break;
            default:
                if (timers.containsKey(itemName)) {
                    selectedTimer = itemName;
                }
                break;
        }

        updateCentralClock(player);
    }

    private void modifyTimer(int amount) {
        int current = timers.get(selectedTimer);
        int newTime = Math.max(10, current + amount); // Limite minimale de 10 secondes
        timers.put(selectedTimer, newTime);
        Bukkit.getLogger().info("[DEBUG] " + selectedTimer + " mis à jour : " + timers.get(selectedTimer) + "s");

        // 🔄 Met à jour l'affichage de la montre centrale après modification
        updateCentralClock();
    }

}
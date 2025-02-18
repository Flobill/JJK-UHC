package me.jjkuhc.jjkcompass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassGUI implements Listener {
    private JavaPlugin plugin;

    public CompassGUI(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Menu de Téléportation");

        // Fond en vitres colorées
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Ajout du lit rouge (Spawn) en slot 12
        ItemStack spawnItem = new ItemStack(Material.RED_BED);
        ItemMeta spawnMeta = spawnItem.getItemMeta();
        spawnMeta.setDisplayName("§cSe téléporter au Spawn");
        spawnItem.setItemMeta(spawnMeta);
        inv.setItem(12, spawnItem);

        // Ajout de la plume (Jump) en slot 14
        ItemStack jumpItem = new ItemStack(Material.FEATHER);
        ItemMeta jumpMeta = jumpItem.getItemMeta();
        jumpMeta.setDisplayName("§eSe téléporter au Jump");
        jumpItem.setItemMeta(jumpMeta);
        inv.setItem(14, jumpItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§6Menu de Téléportation")) {
            event.setCancelled(true); // Empêche les joueurs de déplacer les items

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.RED_BED) {
                teleportPlayer(player, "spawn");
            } else if (clickedItem.getType() == Material.FEATHER) {
                teleportPlayer(player, "jump");
            }
        }
    }

    private void teleportPlayer(Player player, String target) {
        if (!plugin.getConfig().contains(target)) {
            player.sendMessage("§cLa position de " + target + " n’a pas été définie !");
            return;
        }

        double x = plugin.getConfig().getDouble(target + ".x");
        double y = plugin.getConfig().getDouble(target + ".y");
        double z = plugin.getConfig().getDouble(target + ".z");
        float yaw = (float) plugin.getConfig().getDouble(target + ".yaw");
        float pitch = (float) plugin.getConfig().getDouble(target + ".pitch");

        Location loc = new Location(player.getWorld(), x, y, z, yaw, pitch);
        player.teleport(loc);
        player.sendMessage("§aTéléportation au " + target + " !");
    }
}
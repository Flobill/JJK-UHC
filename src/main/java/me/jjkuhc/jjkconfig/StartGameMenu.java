package me.jjkuhc.jjkconfig;

import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameState;
import me.jjkuhc.host.HostManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StartGameMenu implements Listener {

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Lancement de la Partie");

        // Fond en vitres colorées
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Laine dynamique verte ou rouge en fonction de l'état de la partie
        ItemStack wool = new ItemStack(GameManager.isState(GameState.EN_ATTENTE) ? Material.LIME_WOOL : Material.RED_WOOL);
        ItemMeta woolMeta = wool.getItemMeta();
        woolMeta.setDisplayName(GameManager.isState(GameState.EN_ATTENTE) ? "§aLancer la Partie" : "§cTerminer la Partie");
        wool.setItemMeta(woolMeta);
        inv.setItem(13, wool);

        // Flèche de retour
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        arrowMeta.setDisplayName("§7Retour");
        arrow.setItemMeta(arrowMeta);
        inv.setItem(26, arrow);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Lancement de la Partie")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Gestion de la laine verte/rouge
        if ((clickedItem.getType() == Material.LIME_WOOL || clickedItem.getType() == Material.RED_WOOL)) {
            if (!HostManager.isHost(player)) {
                player.sendMessage("§cSeul l'host peut lancer ou terminer la partie !");
                return;
            }

            if (GameManager.isState(GameState.EN_ATTENTE)) {
                player.performCommand("jjkstart");
            } else if (GameManager.isState(GameState.EN_COURS)) {
                GameManager.setCurrentState(GameState.FINIE);
                Bukkit.broadcastMessage("§cLa partie a été terminée par l'hôte !");
            }
        }

        // Gestion de la flèche de retour
        if (clickedItem.getType() == Material.ARROW) {
            new ConfigMenu().open(player);
        }
    }
}
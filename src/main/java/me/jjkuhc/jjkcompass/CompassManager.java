package me.jjkuhc.jjkcompass;

import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassManager implements Listener {
    private JavaPlugin plugin;

    public CompassManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Vérifie que la partie est en attente avant de donner la boussole
        if (GameManager.isState(GameState.EN_ATTENTE)) {
            giveCompass(player);
        }
    }

    private void giveCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("§6Boussole de Navigation");
        compass.setItemMeta(meta);

        player.getInventory().setItem(4, compass); // 5ᵉ slot (index 4)
    }

    @EventHandler
    public void onCompassClick(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.COMPASS) return;

        Player player = event.getPlayer();

        // Vérifie si la partie est en attente
        if (!GameManager.isState(GameState.EN_ATTENTE)) {
            player.sendMessage("§cVous ne pouvez pas utiliser la boussole une fois la partie lancée !");
            return;
        }

        new CompassGUI(plugin).open(player);
    }
}
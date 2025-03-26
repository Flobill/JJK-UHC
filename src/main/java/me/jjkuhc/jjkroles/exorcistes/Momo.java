package me.jjkuhc.jjkroles.exorcistes;

import me.jjkuhc.jjkgame.EnergyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Momo implements Listener {

    private static final int MAX_ENERGIE_OCCULTE = 600;
    private final Player player;
    private static final int FURTIVITE_COST = 1; // 1 énergie par seconde
    private static final int BALAI_COST = 500;
    private static final int DETECTION_RAYON = 20; // Portée de détection des fléaux
    public static final Map<UUID, Momo> momoInstances = new HashMap<>();
    private BukkitRunnable furtiviteTask;
    private final Set<UUID> fleauxCroises = new HashSet<>();


    public Momo(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            EnergyManager.setEnergy(player, MAX_ENERGIE_OCCULTE);
            EnergyManager.setMaxEnergy(player, MAX_ENERGIE_OCCULTE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            momoInstances.put(player.getUniqueId(), this);
            giveBalaiItem();
            startFurtiviteSurveillance();
        }
    }

    private void giveBalaiItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "🧹 Balai de Paille");
            item.setItemMeta(meta);
        }
        player.getInventory().addItem(item);
    }

    public void startFurtiviteSurveillance() {
        furtiviteTask = new BukkitRunnable() {
            @Override
            public void run() {
                boolean aArmure = false;
                for (ItemStack item : player.getInventory().getArmorContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        aArmure = true;
                        break;
                    }
                }

                if (!aArmure) {
                    // ✅ Si elle a pas d'armure, elle devient invisible et consomme
                    player.setInvisible(true);
                    if (EnergyManager.getEnergy(player) >= FURTIVITE_COST) {
                        EnergyManager.reduceEnergy(player, FURTIVITE_COST);
                    } else {
                        player.sendMessage(ChatColor.RED + "❌ Plus assez d'énergie pour rester furtive !");
                        player.setInvisible(false);
                        cancel(); // Stoppe la furtivité si plus d'énergie
                    }
                } else {
                    // ✅ Elle porte une armure, elle devient visible
                    player.setInvisible(false);
                }
            }
        };
        furtiviteTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 20L); // Vérifie toutes les secondes
    }

    public void activerBalai() {
        if (EnergyManager.getEnergy(player) < BALAI_COST) {
            player.sendMessage(ChatColor.RED + "❌ Pas assez d'énergie occulte !");
            return;
        }

        // ✅ Vérifie la voie libre
        Location loc = player.getLocation();
        for (int y = 1; y <= 30; y++) {
            Location check = loc.clone().add(0, y, 0);
            if (!check.getBlock().isPassable()) {
                player.sendMessage(ChatColor.RED + "❌ La voie est obstruée au-dessus !");
                return;
            }
        }

        // ✅ Consomme l'énergie et lance
        EnergyManager.reduceEnergy(player, BALAI_COST);
        player.sendMessage(ChatColor.GOLD + "🧹 Tu t'envoles avec le Balai de Paille !");

        // ✅ TP 30 blocs plus haut
        Location start = loc.clone().add(0, 30, 0);
        player.teleport(start);

        // ✅ Simule un planage sur 80 blocs
        Vector direction = player.getLocation().getDirection().normalize().multiply(1.2); // Ajuste la vitesse
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 80) { // Simule 80 blocs
                    cancel();
                    return;
                }
                player.setVelocity(direction);
                ticks++;
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 2L); // Toutes les 2 ticks pour le contrôle

        // ✅ Immunité dégâts de chute pendant 15 sec
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15 * 20, 4, false, false));
    }

    @EventHandler
    public void onBalaiClick(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;
        if (!item.hasItemMeta() || !item.getItemMeta().getDisplayName().contains("Balai de Paille")) return;

        event.setCancelled(true); // ✅ Bloque l'utilisation classique de la Nether Star
        activerBalai(); // ✅ Lancer la capacité
    }

    public void startDetectionNuit() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!me.jjkuhc.jjkconfig.EpisodeManager.isDay()) { // On vérifie qu'on est bien la nuit
                    for (Player cible : Bukkit.getOnlinePlayers()) {
                        if (cible.equals(player)) continue;
                        if (me.jjkuhc.jjkgame.GameManager.getPlayerCamp(cible).toString().equals("FLEAUX")) {
                            if (cible.getLocation().distance(player.getLocation()) <= DETECTION_RAYON) {
                                fleauxCroises.add(cible.getUniqueId());
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 20L); // Scan toutes les secondes
    }

    public void envoyerResultatDetection() {
        player.sendMessage(ChatColor.LIGHT_PURPLE + "🌙 Pendant la nuit, tu as croisé " + fleauxCroises.size() + " fléau(x).");
        fleauxCroises.clear(); // Reset pour la prochaine nuit
    }

}
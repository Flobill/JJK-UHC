package me.jjkuhc.jjkroles.fleaux;

import me.jjkuhc.jjkgame.EnergyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Geto implements Listener {

    private static final int MAX_ENERGIE_OCCULTE = 1000;
    private final Player player;
    private boolean mortSimulee = false;
    private long lastComboTime = 0;
    private static final int COMBO_COOLDOWN_GLOBAL = 45; // en secondes
    private final HashMap<UUID, Integer> comboCounter = new HashMap<>();
    private static final Map<UUID, Geto> getoInstances = new HashMap<>();
    private boolean revele = false;
    private BukkitRunnable surveillanceTache;
    private boolean lisereUtilise = false;
    private static final int LISERE_COUT = 1000;
    private static final int LISERE_RAYON = 30;
    private static final int LISERE_DELAY_MINUTES = 1; // 30 minutes avant de l'utiliser
    private static final int LISERE_DUREE = 5 * 60; // 5 minutes en secondes
    private static final int LISERE_DUREE_GOJO = 3 * 60; // 3 minutes pour Gojo
    private long lisereDisponibleApres = 0; // Pour timer l'activation

    public Geto(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            EnergyManager.setEnergy(player, 0);
            EnergyManager.setMaxEnergy(player, MAX_ENERGIE_OCCULTE);
            applyPermanentEffects();
            giveAbilityItem();
            verifierForce();
            getoInstances.put(player.getUniqueId(), this);
            // Lisère disponible après X minutes de jeu
            lisereDisponibleApres = System.currentTimeMillis() + (LISERE_DELAY_MINUTES * 60 * 1000);
        }
    }

    public static Geto getGetoInstance(Player player) {
        return getoInstances.get(player.getUniqueId());
    }

    // ✅ Effets passifs
    private void applyPermanentEffects() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
    }

    private void verifierForce() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                boolean fléauProche = false;

                for (Player nearby : player.getWorld().getPlayers()) {
                    if (nearby.equals(player)) continue;
                    if (nearby.getLocation().distance(player.getLocation()) <= 5) {
                        // ✅ Vérifie si le joueur proche est un Fléau
                        if (me.jjkuhc.jjkgame.GameManager.getPlayerCamp(nearby).toString().equals("FLEAUX")) {
                            fléauProche = true;
                            break;
                        }
                    }
                }

                if (!fléauProche) {
                    // ✅ Ajoute Force 1 si aucun Fléau proche
                    if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 0, false, false));
                    }
                } else {
                    // ✅ Enlève Force si un Fléau est proche
                    player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 20L); // Vérifie toutes les secondes
    }

    // ✅ Donne la Nether Star pour Lisère du Supplice
    private void giveAbilityItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "🌀 Lisère du Supplice");
            item.setItemMeta(meta);
        }
        player.getInventory().addItem(item);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;
        if (!item.hasItemMeta() || !item.getItemMeta().getDisplayName().contains("Lisère du Supplice")) return;

        event.setCancelled(true); // On empêche toute autre interaction avec la star
        activerLisereDuSupplice(); // ✅ On déclenche la capacité
    }

    @EventHandler
    public void onGetoAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        if (!attacker.equals(player)) return; // Seul Geto déclenche son effet

        if (mortSimulee) {
            declencherRevelation();
        }

        // ✅ Check cooldown global
        if (System.currentTimeMillis() - lastComboTime < COMBO_COOLDOWN_GLOBAL * 1000) {
            return;
        }

        UUID targetID = target.getUniqueId();
        comboCounter.put(targetID, comboCounter.getOrDefault(targetID, 0) + 1);

        if (comboCounter.get(targetID) >= 3) {
            // ✅ Combo réussi : Nausée
            target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0)); // 10 sec
            player.sendMessage(ChatColor.GOLD + "Combo réussi ! " + target.getName() + " subit Nausée !");
            target.sendMessage(ChatColor.RED + "Vous subissez Nausée suite au combo de Geto !");

            // ✅ Reset combo et démarrer le cooldown global
            comboCounter.clear();
            lastComboTime = System.currentTimeMillis();
        } else {
            // ✅ Reset combo au bout de 5 sec si pas de suite
            new BukkitRunnable() {
                @Override
                public void run() {
                    comboCounter.remove(targetID);
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 5 * 20L);
        }
    }

    public void simulerMort() {
        if (mortSimulee) {
            player.sendMessage(ChatColor.RED + "Vous avez déjà simulé votre mort !");
            return;
        }

        mortSimulee = true;
        me.jjkuhc.jjkgame.DeathManager.broadcastDeathMessage(player);
        player.sendMessage(ChatColor.GRAY + "Vous avez simulé votre mort. Enlève ton armure pour devenir invisible.");

        // Enlève le joueur du TAB
        player.setPlayerListName("");

        // Démarre la surveillance armure
        startSurveillanceMort();
    }

    public void startSurveillanceMort() {
        surveillanceTache = new BukkitRunnable() {
            @Override
            public void run() {
                if (!mortSimulee) {
                    cancel();
                    return;
                }

                boolean porteArmure = false;
                for (ItemStack armor : player.getInventory().getArmorContents()) {
                    if (armor != null && armor.getType() != Material.AIR) {
                        porteArmure = true;
                        break;
                    }
                }

                // Si pas d'armure et pas "révélé" → Invisible
                if (mortSimulee && !porteArmure && !revele) {
                    player.setInvisible(true);
                } else {
                    player.setInvisible(false);
                }
            }
        };
        surveillanceTache.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 40L); // Check toutes les 2s
    }

    public void declencherRevelation() {
        if (revele) return;

        revele = true;
        player.setInvisible(false);
        player.sendMessage(ChatColor.RED + "⚠ Vous avez été révélé pendant 1 minute !");

        new BukkitRunnable() {
            @Override
            public void run() {
                revele = false;
                player.sendMessage(ChatColor.GRAY + "⚠ Vous pouvez de nouveau redevenir invisible en retirant votre armure.");

                // ✅ Vérification auto pour redevenir invisible si pas d'armure
                boolean porteArmure = false;
                for (ItemStack armor : player.getInventory().getArmorContents()) {
                    if (armor != null && armor.getType() != Material.AIR) {
                        porteArmure = true;
                        break;
                    }
                }
                if (!porteArmure) {
                    player.setInvisible(true);
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 20L * 60); // 1 min
    }

    public void activerLisereDuSupplice() {
        if (lisereUtilise) {
            player.sendMessage(ChatColor.RED + "❌ Lisère du Supplice a déjà été utilisé !");
            return;
        }

        if (System.currentTimeMillis() < lisereDisponibleApres) {
            long reste = (lisereDisponibleApres - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.RED + "Lisère du Supplice sera disponible dans " + reste / 60 + " min.");
            return;
        }

        if (EnergyManager.getEnergy(player) < LISERE_COUT) {
            player.sendMessage(ChatColor.RED + "❌ Pas assez d'énergie occulte !");
            return;
        }

        // ✅ Ouvre un inventaire de sélection des joueurs dans le rayon
        ouvrirMenuSelection();
    }

    public void scellerCible(Player cible) {
        lisereUtilise = true;
        EnergyManager.reduceEnergy(player, LISERE_COUT);

        // ✅ Retire toutes les Nether Stars du joueur et stocke-les
        List<ItemStack> netherStars = new ArrayList<>();
        for (ItemStack item : cible.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_STAR) {
                netherStars.add(item.clone());
                cible.getInventory().remove(item);
            }
        }

        // Détermine la durée selon si c’est Gojo ou non
        int duree = (me.jjkuhc.jjkgame.GameManager.getPlayerRole(cible).toString().equals("GOJO")) ? LISERE_DUREE_GOJO : LISERE_DUREE;

        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "🌀 " + cible.getName() + " est scellé par Lisère du Supplice pendant " + (duree / 60) + " minutes !");
        cible.sendMessage(ChatColor.RED + "❌ Vous êtes scellé et ne pouvez plus utiliser vos capacités !");

        cible.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duree * 20, 2, false, false));

        new BukkitRunnable() {
            @Override
            public void run() {
                // ✅ Fin du scellage
                Bukkit.broadcastMessage(ChatColor.GREEN + cible.getName() + " est libéré du Lisère du Supplice !");
                cible.removePotionEffect(PotionEffectType.SLOW);
                // ✅ Redonne les Nether Stars
                for (ItemStack star : netherStars) {
                    cible.getInventory().addItem(star);
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), duree * 20L);
    }

    public void ouvrirMenuSelection() {
        Inventory menu = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Sélectionnez une cible");

        for (Player cible : Bukkit.getOnlinePlayers()) {
            if (cible.equals(player)) continue; // On skip Geto lui-même
            if (cible.getLocation().distance(player.getLocation()) > LISERE_RAYON) continue; // Hors rayon

            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerItem.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(cible);
                meta.setDisplayName(ChatColor.GOLD + cible.getName());
                playerItem.setItemMeta(meta);
            }
            menu.addItem(playerItem);
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();

        if (!clicker.equals(player)) return;
        if (event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Sélectionnez une cible")) {
            event.setCancelled(true); // ✅ Bloque la prise des têtes

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            Player cible = meta.getOwningPlayer().getPlayer();
            if (cible != null) {
                clicker.closeInventory();
                scellerCible(cible); // ✅ On scelle la cible directement
            }
        }
    }

}

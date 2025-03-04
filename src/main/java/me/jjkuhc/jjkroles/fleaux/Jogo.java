package me.jjkuhc.jjkroles.fleaux;

import me.jjkuhc.jjkgame.EnergyManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.UUID;

public class Jogo implements Listener {
    private static final int MAX_ENERGIE_OCCULTE = 700;
    private static final int SURCHAUFFE_COUT = 400;
    private static final int SURCHAUFFE_RAYON = 15;
    private static final int SURCHAUFFE_DUREE = 10;
    private static final int SURCHAUFFE_COOLDOWN = 480; // 8 minutes en secondes
    private static final int COMBAT_COUT_PAR_SECONDE = 8;
    private static final HashSet<UUID> joueursEnFeu = new HashSet<>();
    private static final HashSet<UUID> joueursNotifies = new HashSet<>();

    private final Player player;
    private boolean modeCombatActif = false;
    private BukkitTask modeCombatTask;
    private long lastSurchauffeTime = 0;

    public Jogo(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            EnergyManager.setEnergy(player, MAX_ENERGIE_OCCULTE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
            donnerNetherStar();
            verifierForceDeNuit();
        }
    }

    private void donnerNetherStar() {
        ItemStack surchauffeItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = surchauffeItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "🔥 Surchauffe");
            surchauffeItem.setItemMeta(meta);
        }
        player.getInventory().addItem(surchauffeItem);
    }

    private void verifierForceDeNuit() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                long time = player.getWorld().getTime();
                boolean estLaNuit = (time >= 13000 && time <= 23000);

                if (estLaNuit) {
                    if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2200, 0, false, false));
                    }
                } else {
                    player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 20L); // Vérification toutes les secondes
    }

    @EventHandler
    public void onSurchauffeUse(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;

        if (event.getAction().toString().contains("RIGHT")) {
            activerSurchauffe();
        } else if (event.getAction().toString().contains("LEFT")) {
            if (modeCombatActif) {
                desactiverModeCombat(); // ✅ Désactive le mode combat si actif
            } else {
                activerModeCombat(); // ✅ Sinon, l’active normalement
            }
        }
    }

    private void activerSurchauffe() {
        long tempsRestant = (SURCHAUFFE_COOLDOWN * 1000) - (System.currentTimeMillis() - lastSurchauffeTime);

        if (tempsRestant > 0) {
            long secondesRestantes = tempsRestant / 1000;
            player.sendMessage(ChatColor.RED + "❌ Surchauffe est encore en recharge ! (" + secondesRestantes + "s restantes)");
            return;
        }

        if (EnergyManager.getEnergy(player) < SURCHAUFFE_COUT) {
            player.sendMessage(ChatColor.RED + "❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, SURCHAUFFE_COUT);
        lastSurchauffeTime = System.currentTimeMillis();

        player.sendMessage(ChatColor.GOLD + "🔥 Vous activez Surchauffe !");

        for (Player cible : Bukkit.getOnlinePlayers()) {
            if (cible.equals(player)) continue;
            if (cible.getLocation().distance(player.getLocation()) <= SURCHAUFFE_RAYON) {
                cible.setFireTicks(SURCHAUFFE_DUREE * 20);
                cible.sendMessage(ChatColor.RED + "🔥 Vous êtes en feu à cause de Surchauffe !");

                joueursEnFeu.add(cible.getUniqueId());
            }
        }

        maintenirFeu();

        new BukkitRunnable() {
            @Override
            public void run() {
                joueursEnFeu.clear();
                joueursNotifies.clear();
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), SURCHAUFFE_DUREE * 20);
    }

        @EventHandler
    public void onPlayerTryExtinguish(PlayerMoveEvent event) {
        Player cible = event.getPlayer();

        if (joueursEnFeu.contains(cible.getUniqueId())) {
            Location loc = cible.getLocation();
            Material blockType = loc.getBlock().getType();

            // ✅ Vérifier si le joueur touche de l'eau
            if (blockType == Material.WATER || blockType == Material.CAULDRON) {
                event.setCancelled(true); // ❌ Empêcher le mouvement vers l’eau

                // ✅ Envoyer le message une seule fois
                if (!joueursNotifies.contains(cible.getUniqueId())) {
                    cible.sendMessage(ChatColor.RED + "🔥 Impossible d'éteindre le feu de Surchauffe !");
                    joueursNotifies.add(cible.getUniqueId());
                }

                // ✅ Remettre immédiatement le feu
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
                    cible.setFireTicks(200); // 10 secondes de feu
                }, 1L);
            }
        }
    }

    private void maintenirFeu() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (joueursEnFeu.isEmpty()) {
                    cancel();
                    return;
                }

                for (UUID uuid : joueursEnFeu) {
                    Player cible = Bukkit.getPlayer(uuid);
                    if (cible != null && cible.isOnline() && cible.getFireTicks() <= 0) {
                        cible.setFireTicks(200); // ✅ Remettre le feu s'il s'est éteint
                        cible.sendMessage(ChatColor.RED + "🔥 Impossible d'éteindre le feu de Surchauffe !");
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 20L, 20L); // Vérifie chaque seconde
    }

    private void activerModeCombat() {
        if (modeCombatActif) {
            player.sendMessage(ChatColor.RED + "❌ Mode combat déjà activé !");
            return;
        }

        modeCombatActif = true;
        player.sendMessage(ChatColor.GOLD + "🔥 Mode combat activé !");
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false));

        // ✅ Appliquer Fire Aspect uniquement aux épées et Flame uniquement aux arcs
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType().name().contains("SWORD")) {
                item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
            } else if (item.getType().name().contains("BOW")) {
                item.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
            }
        }

        modeCombatTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (EnergyManager.getEnergy(player) < COMBAT_COUT_PAR_SECONDE) {
                    desactiverModeCombat();
                    return;
                }
                EnergyManager.reduceEnergy(player, COMBAT_COUT_PAR_SECONDE);
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 20L, 20L);
    }

    private void desactiverModeCombat() {
        if (!modeCombatActif) return;

        modeCombatActif = false;
        player.sendMessage(ChatColor.RED + "❌ Mode combat désactivé !");
        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                item.removeEnchantment(Enchantment.FIRE_ASPECT);
                item.removeEnchantment(Enchantment.ARROW_FIRE);
            }
        }

        if (modeCombatTask != null) {
            modeCombatTask.cancel();
        }
    }
}
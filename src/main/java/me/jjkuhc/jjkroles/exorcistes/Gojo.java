package me.jjkuhc.jjkroles.exorcistes;

import me.jjkuhc.jjkgame.EnergyManager;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Gojo implements Listener {
    private final Player player;
    private final Set<UUID> cooldowns = new HashSet<>();
    private boolean hasUsedMurasaki = false;
    private boolean bandeauOnCooldown = false;
    private long cooldownStartTime = -1;

    public Gojo(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            applyPermanentEffects();
            revealMegumi();
            EnergyManager.setEnergy(player, 1500);
            giveAbilityItem();
        }
    }

    // ✅ Effets permanents
    private void applyPermanentEffects() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
    }

    // ✅ Révéler Megumi
    private void revealMegumi() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        Player megumiPlayer = null;

        for (Player onlinePlayer : onlinePlayers) {
            if (GameManager.getPlayerRole(onlinePlayer) == RoleType.MEGUMI) {
                megumiPlayer = onlinePlayer;
                break;
            }
        }

        if (megumiPlayer != null) {
            onlinePlayers.remove(megumiPlayer);
            Collections.shuffle(onlinePlayers);
            List<Player> selectedPlayers = new ArrayList<>();
            int playersToSelect = Math.min(3, onlinePlayers.size());
            selectedPlayers.addAll(onlinePlayers.subList(0, playersToSelect));
            selectedPlayers.add(megumiPlayer);
            Collections.shuffle(selectedPlayers);

            player.sendMessage("§a🔍 Voici les joueurs suspectés d'être Megumi :");
            for (Player p : selectedPlayers) {
                player.sendMessage(" - §b" + p.getName());
            }
        }
    }

    //Donne les nether stars
    private void giveAbilityItem() {
        // Première Nether Star : Pouvoirs
        ItemStack abilityStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta abilityMeta = abilityStar.getItemMeta();
        abilityMeta.setDisplayName("§bPouvoirs de Satoru Gojo");
        abilityStar.setItemMeta(abilityMeta);
        player.getInventory().addItem(abilityStar);

        // Deuxième Nether Star : Bandeau
        ItemStack bandeauStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta bandeauMeta = bandeauStar.getItemMeta();
        bandeauMeta.setDisplayName("§9Bandeau de Gojo");
        bandeauStar.setItemMeta(bandeauMeta);
        player.getInventory().addItem(bandeauStar);

        player.sendMessage("§a🌟 Vous avez reçu vos pouvoirs et votre bandeau !");
    }

    // ✅ Activation des capacités avec différents clics
// ✅ Gestion unique des clics
    @EventHandler
    public void onAbilityUse(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;

        String itemName = item.getItemMeta().getDisplayName();
        Action action = event.getAction();

        // ✅ Gestion des pouvoirs de Gojo
        if (itemName.equals("§bPouvoirs de Satoru Gojo")) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                if (player.isSneaking()) {
                    if (!hasUsedMurasaki) {
                        useMurasaki();
                    } else {
                        player.sendMessage("§c⚠ Vous avez déjà utilisé Murasaki !");
                    }
                } else {
                    useRepulsion();
                }
            } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                useTeleport();
            }
        }

        // ✅ Gestion du bandeau de Gojo
        if (itemName.equals("§9Bandeau de Gojo")) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                activateBandeau();
            }
        }
    }

    // ✅ Repulsion - Clic droit
    private void useRepulsion() {
        if (EnergyManager.getEnergy(player) < 450) {
            player.sendMessage("§cPas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 450);
        player.sendMessage("§b💥 Vous avez utilisé Repulsion !");

        // ✅ Effets visuels et sonores
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(), 100, new Particle.DustOptions(Color.RED, 1.0f));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // ✅ Dégâts et knockback sur les joueurs proches
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player) && target.getLocation().distance(player.getLocation()) <= 15) {
                double damage = 2.0; // 1 cœur = 2 points de vie
                double newHealth = Math.max(0, target.getHealth() - damage);
                target.setHealth(newHealth);
                Vector knockback = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(2);
                target.setVelocity(knockback);
                target.sendMessage("§c💥 Vous avez été repoussé par Gojo !");
            }
        }
    }

    // ✅ Capacité : Téléportation
    private void useTeleport() {
        if (EnergyManager.getEnergy(player) < 450) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        Player target = getNearestPlayer(20);
        if (target == null) {
            player.sendMessage("§cAucun joueur à proximité !");
            return;
        }

        EnergyManager.reduceEnergy(player, 450);
        target.teleport(player.getLocation().add(0, 1, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));

        player.sendMessage("§b🔵 " + target.getName() + " a été téléporté devant vous !");
    }

    // ✅ Capacité ultime : Murasaki
    private void useMurasaki() {
        if (EnergyManager.getEnergy(player) < 900) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        hasUsedMurasaki = true;
        EnergyManager.reduceEnergy(player, 900);
        player.sendMessage("§d💥 Murasaki en chargement...");

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation(), 200);

                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!target.equals(player) && target.getLocation().distance(player.getLocation()) <= 100) {
                        double murasakiDamage = 8.0; // 4 cœurs = 8 points de vie
                        double newHealth = Math.max(0, target.getHealth() - murasakiDamage);
                        target.setHealth(newHealth);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 6000, 0));
                    }
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 60L); // 3 sec
    }

    // ✅ Chercher le joueur le plus proche
    private Player getNearestPlayer(double radius) {
        Player nearest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player)) {
                double distance = target.getLocation().distance(player.getLocation());
                if (distance <= radius && distance < closestDistance) {
                    closestDistance = distance;
                    nearest = target;
                }
            }
        }
        return nearest;
    }

    // ✅ Activation du bandeau
    private void activateBandeau() {
        if (bandeauOnCooldown) {
            player.sendMessage("§c⏳ Le bandeau est encore en cooldown !");
            return;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3000, 1)); // Speed 2 pendant 2m30
        player.sendMessage("§9🏃 Vous avez activé votre bandeau !");

        // ✅ Lancer le cooldown de 24000 ticks (un cycle jour/nuit)
        bandeauOnCooldown = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                bandeauOnCooldown = false;
                player.sendMessage("§a🔄 Votre bandeau est à nouveau disponible !");
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 24000L);
    }

    // ✅ Vérifier la fin du cooldown du bandeau
    private void checkBandeauCooldown() {
        if (bandeauOnCooldown) {
            long currentTime = player.getWorld().getTime();
            long timePassed = (currentTime >= cooldownStartTime)
                    ? currentTime - cooldownStartTime
                    : (24000 - cooldownStartTime) + currentTime; // Gère le passage de la nuit au jour

            if (timePassed >= 24000) { // Un cycle complet = 24000 ticks
                bandeauOnCooldown = false;
                player.sendMessage("§a✅ Votre bandeau est à nouveau disponible !");
            }
        }
    }

    // ✅ Vérification régulière du cooldown (toutes les 5 secondes)
    public void startCooldownChecker(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkBandeauCooldown();
            }
        }.runTaskTimer(plugin, 0L, 100L); // Vérifie toutes les 5 secondes (100 ticks)
    }
}
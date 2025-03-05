package me.jjkuhc.jjkroles.fleaux;

import me.jjkuhc.jjkgame.EnergyManager;
import org.bukkit.*;
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

import java.util.*;

public class Hanami implements Listener {
    private static final int MAX_ENERGIE_OCCULTE = 1000;
    private static final int NAUSEE_SOMBRE_COUT = 750;
    private static final int SYLVE_LUGUBRE_COUT = 1000;
    private static final int BOURGEON_UTILISATIONS_MAX = 2;

    private final Player player;
    private int bourgeonUtilisations = 0;
    private final HashSet<UUID> joueursMaudits = new HashSet<>();
    private final HashMap<UUID, ItemStack> removedItems = new HashMap<>();
    private final HashMap<UUID, Collection<PotionEffect>> storedEffects = new HashMap<>();
    private static final HashMap<UUID, Double> coeursPerdus = new HashMap<>();

    public Hanami(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            EnergyManager.setEnergy(player, MAX_ENERGIE_OCCULTE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
            donnerNetherStar();
        }
    }

    private void donnerNetherStar() {
        // Vérifier si le joueur possède déjà une Nether Star
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_STAR) {
                return; // Sortie immédiate pour éviter un doublon
            }
        }

        // Si pas de Nether Star, en donner une
        ItemStack activableItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = activableItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GREEN + "🌿 Pouvoirs de Hanami");
            activableItem.setItemMeta(meta);
        }
        player.getInventory().addItem(activableItem);
    }

    @EventHandler
    public void onAbilityUse(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;

        if (event.getAction().toString().contains("RIGHT")) {
            activerNauseeSombre();
        } else if (event.getAction().toString().contains("LEFT")) {
            activerSylveLugubre();
        }
    }

    public void utiliserBourgeon(Player cible) {
        if (bourgeonUtilisations >= BOURGEON_UTILISATIONS_MAX) {
            player.sendMessage(ChatColor.RED + "❌ Vous ne pouvez plus utiliser Bourgeon !");
            return;
        }

        joueursMaudits.add(cible.getUniqueId());
        player.sendMessage(ChatColor.DARK_GREEN + "🌿 " + cible.getName() + " a été maudit !");

        int energieInitiale = EnergyManager.getEnergy(cible);
        coeursPerdus.putIfAbsent(cible.getUniqueId(), 0.0);

        new BukkitRunnable() {
            private int lastEnergy = energieInitiale;

            @Override
            public void run() {
                if (!joueursMaudits.contains(cible.getUniqueId())) {
                    cancel();
                    return;
                }

                int currentEnergy = EnergyManager.getEnergy(cible);
                if (currentEnergy < lastEnergy) {
                    double perte = 2.0;
                    double nouveauxCoeursPerdus = coeursPerdus.get(cible.getUniqueId()) + perte;
                    coeursPerdus.put(cible.getUniqueId(), nouveauxCoeursPerdus);

                    updateMaxHealthWithBourgeon(cible);
                    cible.sendMessage(ChatColor.RED + "💀 Vous perdez 2 cœurs permanents à cause de la malédiction de Hanami !");
                }

                lastEnergy = currentEnergy;
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 20L, 20L);

        bourgeonUtilisations++;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (joueursMaudits.contains(cible.getUniqueId())) {
                    double recuperation = 2.0;
                    double totalPerdu = coeursPerdus.getOrDefault(cible.getUniqueId(), 0.0);

                    if (totalPerdu > 0) {
                        coeursPerdus.put(cible.getUniqueId(), Math.max(0.0, totalPerdu - recuperation));
                        updateMaxHealthWithBourgeon(cible);
                        player.sendMessage(ChatColor.GREEN + "🌱 " + cible.getName() + " a récupéré 1 cœur.");
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 20L * 60 * 20, 20L * 60 * 20);
    }

    private void activerNauseeSombre() {
        if (EnergyManager.getEnergy(player) < NAUSEE_SOMBRE_COUT) {
            player.sendMessage(ChatColor.RED + "❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, NAUSEE_SOMBRE_COUT);
        player.sendMessage(ChatColor.DARK_GREEN + "🌫️ Vous avez utilisé Nausée Sombre !");

        for (Player cible : Bukkit.getOnlinePlayers()) {
            if (!cible.equals(player) && cible.getLocation().distance(player.getLocation()) <= 15) {
                cible.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 300, 0));
                cible.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
                cible.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));

                if (EnergyManager.getMaxEnergy(cible) < MAX_ENERGIE_OCCULTE) {
                    // Retire la Nether Star et la stocke
                    for (ItemStack item : cible.getInventory().getContents()) {
                        if (item != null && item.getType() == Material.NETHER_STAR) {
                            removedItems.put(cible.getUniqueId(), item);
                            cible.getInventory().remove(item);
                            cible.sendMessage(ChatColor.RED + "❌ Votre pouvoir a été scellé pendant 5 minutes !");
                            break;
                        }
                    }

                    // Planifie la restitution après 5 minutes
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (removedItems.containsKey(cible.getUniqueId())) {
                                cible.getInventory().addItem(removedItems.get(cible.getUniqueId()));
                                removedItems.remove(cible.getUniqueId());
                                cible.sendMessage(ChatColor.GREEN + "✅ Votre pouvoir vous a été rendu !");
                            }
                        }
                    }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 1 * 60 * 20); // 5 minutes en ticks
                }
            }
        }
    }

    private void activerSylveLugubre() {
        if (EnergyManager.getEnergy(player) < SYLVE_LUGUBRE_COUT) {
            player.sendMessage(ChatColor.RED + "❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, SYLVE_LUGUBRE_COUT);
        player.sendMessage(ChatColor.DARK_GREEN + "🌳 Vous avez activé Sylve Lugubre !");

        World hanamiWorld = Bukkit.getWorld("Hanami");
        if (hanamiWorld == null) {
            player.sendMessage(ChatColor.RED + "❌ Le monde 'Hanami' n'existe pas !");
            return;
        }

        Location spawnLocation = hanamiWorld.getSpawnLocation();
        List<Player> playersToTeleport = new ArrayList<>();

        for (Player cible : Bukkit.getOnlinePlayers()) {
            if (cible.getLocation().distance(player.getLocation()) <= 10) {
                playersToTeleport.add(cible);

                // Stocker et retirer les effets
                storedEffects.put(cible.getUniqueId(), new ArrayList<>(cible.getActivePotionEffects()));
                cible.getActivePotionEffects().forEach(effect -> cible.removePotionEffect(effect.getType()));

                if (!cible.equals(player)) {
                    cible.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1200, 0)); // 1 min
                }

                // Retirer la Nether Star
                for (ItemStack item : cible.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.NETHER_STAR) {
                        removedItems.put(cible.getUniqueId(), item);
                        cible.getInventory().remove(item);
                        break;
                    }
                }

                // Téléportation aléatoire dans une zone de 30x30 blocs
                double randomX = spawnLocation.getX() + (Math.random() * 30) - 15;
                double randomZ = spawnLocation.getZ() + (Math.random() * 30) - 15;
                Location randomLocation = new Location(hanamiWorld, randomX, spawnLocation.getY(), randomZ);
                cible.teleport(randomLocation);

                cible.sendMessage(ChatColor.DARK_RED + "🌲 Vous avez été capturé dans l'extension de Hanami !");
            }
        }

        double randomX = spawnLocation.getX() + (Math.random() * 30) - 15;
        double randomZ = spawnLocation.getZ() + (Math.random() * 30) - 15;
        Location hanamiLocation = new Location(hanamiWorld, randomX, spawnLocation.getY(), randomZ);
        player.teleport(hanamiLocation);

        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.sendMessage(ChatColor.RED + "❌ Vous avez perdu votre Résistance !");

        // Rétablir les joueurs après 1 minute
        new BukkitRunnable() {
            @Override
            public void run() {
                World uhcWorld = Bukkit.getWorld("uhc");
                if (uhcWorld == null) {
                    player.sendMessage(ChatColor.RED + "❌ Erreur : Monde UHC introuvable !");
                    return;
                }

                Location uhcSpawn = uhcWorld.getWorldBorder().getCenter();
                double borderSize = uhcWorld.getWorldBorder().getSize();

                for (Player cible : playersToTeleport) {
                    // Téléportation aléatoire dans le monde UHC
                    double randomX = uhcSpawn.getX() + (Math.random() * borderSize / 2) - (borderSize / 4);
                    double randomZ = uhcSpawn.getZ() + (Math.random() * borderSize / 2) - (borderSize / 4);
                    int highestY = uhcWorld.getHighestBlockYAt((int) randomX, (int) randomZ) + 1;
                    Location randomLocation = new Location(uhcWorld, randomX, highestY, randomZ);
                    cible.teleport(randomLocation);

                    // Restaurer les effets
                    if (storedEffects.containsKey(cible.getUniqueId())) {
                        for (PotionEffect effect : storedEffects.get(cible.getUniqueId())) {
                            cible.addPotionEffect(effect);
                        }
                        storedEffects.remove(cible.getUniqueId());
                    }

                    // Rendre la Nether Star
                    if (removedItems.containsKey(cible.getUniqueId())) {
                        cible.getInventory().addItem(removedItems.get(cible.getUniqueId()));
                        removedItems.remove(cible.getUniqueId());
                    }

                    cible.removePotionEffect(PotionEffectType.SLOW);
                    cible.sendMessage(ChatColor.GREEN + "✅ Vous avez quitté l'extension de Hanami !");
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
                player.sendMessage(ChatColor.GREEN + "✅ Vous avez retrouvé votre Résistance !");
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 1200L); // 1 minute
    }

    public void updateMaxHealthWithBourgeon(Player cible) {
        double baseHealth = cible.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double perte = coeursPerdus.getOrDefault(cible.getUniqueId(), 0.0);

        double newHealth = Math.max(4.0, baseHealth - perte);
        cible.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
    }

    public static double getCoeursPerdus(Player player) {
        return coeursPerdus.getOrDefault(player.getUniqueId(), 0.0);
    }
}
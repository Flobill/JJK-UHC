package me.jjkuhc.jjkroles.fleaux;

import me.jjkuhc.jjkgame.EnergyManager;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
    private final Map<UUID, Integer> episodeFinMalédiction = new HashMap<>();
    private static final Map<UUID, Hanami> hanamiInstances = new HashMap<>();
    private long dernierBourgeon = 0L;
    private static final long COOLDOWN_BOURGEON = 6 * 60 * 1000; // 6 minutes

    public Hanami(Player player) {
        this.player = player;
        hanamiInstances.put(player.getUniqueId(), this);

        if (player != null && player.isOnline()) {
            EnergyManager.setEnergy(player, 0);
            EnergyManager.setMaxEnergy(player, MAX_ENERGIE_OCCULTE);
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

        long maintenant = System.currentTimeMillis();
        if (maintenant - dernierBourgeon < COOLDOWN_BOURGEON) {
            long restant = (COOLDOWN_BOURGEON - (maintenant - dernierBourgeon)) / 1000;
            player.sendMessage(ChatColor.RED + "❌ Bourgeon est en recharge pendant encore " + restant + " secondes.");
            return;
        }

        if (joueursMaudits.contains(cible.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "❌ Ce joueur est déjà maudit !");
            return;
        }

        // ✅ MAJ du nombre d'utilisations AVANT de return
        dernierBourgeon = maintenant;
        bourgeonUtilisations++;

        joueursMaudits.add(cible.getUniqueId());
        cible.sendMessage(ChatColor.DARK_RED + "💀 Vous avez été maudit par Hanami !");
        int episodeActuel = me.jjkuhc.jjkconfig.EpisodeManager.getEpisodeCount();
        episodeFinMalédiction.put(cible.getUniqueId(), episodeActuel + 1);
        player.sendMessage(ChatColor.DARK_GREEN + "🌿 " + cible.getName() + " a été maudit !");

        int energieInitiale = EnergyManager.getEnergy(cible);
        coeursPerdus.putIfAbsent(cible.getUniqueId(), 0.0);
        updateMaxHealthWithBourgeon(cible);


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

                if (EnergyManager.getMaxEnergy(cible) < EnergyManager.getMaxEnergy(player)) {
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
                    }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 2 * 60 * 20); // 2 minutes en ticks
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
            }
        }

        // ✅ Ajoute Hanami une seule fois s’il n’est pas déjà dedans
        if (!playersToTeleport.contains(player)) {
            playersToTeleport.add(player);
        }

        for (Player cible : playersToTeleport) {
            // Stocker et retirer les effets
            storedEffects.put(cible.getUniqueId(), new ArrayList<>(cible.getActivePotionEffects()));
            cible.getActivePotionEffects().forEach(effect -> cible.removePotionEffect(effect.getType()));

            if (cible.equals(player)) {
                // Hanami : effets spéciaux
                cible.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                cible.sendMessage(ChatColor.RED + "Vous avez perdu votre Résistance !");
                cible.sendMessage(ChatColor.DARK_GREEN + "Vous avez activé Sylve Lugubre !");
            } else {
                // Autres joueurs : ralentissement, message et retrait de Nether Star
                cible.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1200, 0));
                cible.sendMessage(ChatColor.DARK_RED + "Vous avez été capturé dans l'extension de Hanami !");
            }

            // Retirer la Nether Star
            for (ItemStack item : cible.getInventory().getContents()) {
                if (item != null && item.getType() == Material.NETHER_STAR) {
                    removedItems.put(cible.getUniqueId(), item);
                    cible.getInventory().remove(item);
                    break;
                }
            }

            // Téléportation safe
            Location safeLoc = findSafeLocation(hanamiWorld, spawnLocation, 20);
            cible.teleport(safeLoc);
        }

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

                    if (!cible.equals(player)) {
                        cible.sendMessage(ChatColor.GREEN + "Vous avez quitté l'extension de Hanami !");
                    } else {
                        cible.sendMessage(ChatColor.GREEN + "Vous avez retrouvé votre Résistance !");
                        cible.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
                    }
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 1200L); // 1 minute
    }

    private Location findSafeLocation(World world, Location base, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            double randomX = base.getX() + (Math.random() * 30) - 10;
            double randomZ = base.getZ() + (Math.random() * 30) - 10;
            int y = base.getBlockY();

            // Teste 5 blocs vers le haut pour trouver de l'air
            for (int dy = 0; dy <= 5; dy++) {
                Location testLoc = new Location(world, randomX, y + dy, randomZ);
                if (testLoc.getBlock().getType() == Material.AIR &&
                        testLoc.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                    return testLoc.add(0.5, 0, 0.5); // Centrage sur le bloc
                }
            }
        }

        // Si on ne trouve rien : fallback sur le spawn
        return base;
    }

    public void updateMaxHealthWithBourgeon(Player cible) {
        double perte = coeursPerdus.getOrDefault(cible.getUniqueId(), 0.0);
        double newHealth = Math.max(4.0, 20.0 - perte);
        if (cible.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            cible.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
        }
    }

    public static double getCoeursPerdus(Player player) {
        return coeursPerdus.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void verifierFinMalédictions() {
        int episodeActuel = me.jjkuhc.jjkconfig.EpisodeManager.getEpisodeCount();

        Iterator<UUID> iterator = joueursMaudits.iterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            int fin = episodeFinMalédiction.getOrDefault(id, -1);

            if (episodeActuel >= fin) {
                Player cible = Bukkit.getPlayer(id);
                if (cible != null && cible.isOnline()) {
                    cible.sendMessage(ChatColor.GREEN + "🌱 La malédiction de Hanami a disparu.");
                }

                iterator.remove();
                episodeFinMalédiction.remove(id);
            }
        }
    }

    public static void verifierToutesLesMalédictions() {
        for (Hanami h : hanamiInstances.values()) {
            h.rendreUnCoeurAuxJoueursMaudits();
            h.verifierFinMalédictions();
        }
    }

    public static Hanami getHanamiInstance(Player player) {
        return hanamiInstances.get(player.getUniqueId());
    }

    public void rendreUnCoeurAuxJoueursMaudits() {
        for (UUID id : joueursMaudits) {
            Player cible = Bukkit.getPlayer(id);
            if (cible != null && cible.isOnline()) {
                double perdus = coeursPerdus.getOrDefault(id, 0.0);
                if (perdus > 0) {
                    coeursPerdus.put(id, Math.max(0.0, perdus - 2.0));
                    updateMaxHealthWithBourgeon(cible); // met à jour la vie max du joueur
                    cible.sendMessage(ChatColor.GREEN + "Vous récupérez 1 cœur permanent à la fin de l’épisode.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player target = event.getPlayer();
        World world = target.getWorld();

        // ❌ Empêche de casser dans le monde "Hanami" sauf si c’est Hanami
        if (world.getName().equals("hanami") && GameManager.getPlayerRole(target) != RoleType.HANAMI) {
            event.setCancelled(true);
            target.sendMessage(ChatColor.RED + "❌ Vous ne pouvez pas casser de blocs dans l'extension de Hanami !");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player target = event.getPlayer();
        World world = target.getWorld();

        // ❌ Empêche de poser dans le monde "Hanami" sauf pour Hanami
        if (world.getName().equals("hanami") && GameManager.getPlayerRole(target) != RoleType.HANAMI) {
            event.setCancelled(true);
            target.sendMessage(ChatColor.RED + "❌ Vous ne pouvez pas poser de blocs dans l'extension de Hanami !");
        }
    }

}
package me.jjkuhc.jjkroles.neutres;

import me.jjkuhc.jjkgame.EnergyManager;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkroles.CampManager;
import me.jjkuhc.jjkroles.CampType;
import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.*;

public class Sukuna implements Listener {
    private final Player player;
    private final Plugin plugin;
    private int fingerCount = 0;
    private boolean innateLeftCooldown = false;
    private boolean innateRightCooldown = false;
    private boolean extensionCooldown = false;
    private final Map<UUID, Collection<PotionEffect>> savedEffects = new HashMap<>();
    private static final Map<UUID, Boolean> stealActiveMap = new HashMap<>();

    public Sukuna(Player player, Plugin plugin) {
        this.player = player;
        this.plugin = plugin;

        if (player != null && player.isOnline()) {
            applyPermanentEffects();
            EnergyManager.setEnergy(player, 1500);
            giveCompass();
            startCompassUpdate();
            giveInnateSpellsItem();
        }
    }

    // ✅ Appliquer les effets permanents de Sukuna
    private void applyPermanentEffects() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, false, false)); // Résistance permanente
        applyNightStrength();
    }

    private void applyNightStrength() {
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = player.getWorld();

                // ✅ Force pendant la nuit normale
                if (isNight(world)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 201, 0, false, false));
                }

                // ✅ Force permanente dans le monde de l'extension de Sukuna
                if (world.getName().equalsIgnoreCase("Sukuna")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 201, 0, false, false));
                }
            }
        }.runTaskTimer(plugin, 0L, 200L); // Toutes les 10 secondes
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }

    // ✅ Donner une boussole qui traque les porteurs de doigts
    private void giveCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        meta.setDisplayName("§cBoussole de Sukuna");
        compass.setItemMeta(meta);
        player.getInventory().addItem(compass);
    }

    // ✅ Met à jour la boussole toutes les 30 secondes
    private void startCompassUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateCompass();
            }
        }.runTaskTimer(plugin, 0L, 600L); // Toutes les 30 secondes (600 ticks)
    }

    private void updateCompass() {
        Player target = getNearestPlayerWithFinger();
        if (target != null) {
            player.setCompassTarget(target.getLocation());
        } else {
            player.sendMessage("§cAucun porteur de doigt trouvé !");
        }
    }

    // ✅ Cherche le joueur le plus proche avec un doigt
    private Player getNearestPlayerWithFinger() {
        Player nearest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player) && hasSukunaFinger(target)) {
                double distance = target.getLocation().distance(player.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    nearest = target;
                }
            }
        }
        return nearest;
    }

    // ✅ Vérifie si un joueur possède un doigt de Sukuna
    private boolean hasSukunaFinger(Player target) {
        for (ItemStack item : target.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_WART) {
                return true;
            }
        }
        return false;
    }

    // ✅ Événements pour gérer les doigts de Sukuna
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.getPlayer().equals(player) && event.getItem().getItemStack().getType() == Material.NETHER_WART) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                updateFingerEffects();
            }, 1L);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().equals(player) && event.getItemDrop().getItemStack().getType() == Material.NETHER_WART) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                updateFingerEffects();
            }, 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            if (p.equals(player)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    updateFingerEffects();
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().equals(player)) {
            updateFingerEffects();
        }
    }

    // ✅ Gérer les effets liés aux doigts de Sukuna
    private void updateFingerEffects() {
        int currentFingers = countSukunaFingers();

        if (currentFingers != fingerCount) {
            fingerCount = currentFingers;
            applyFingerEffects();
        }
    }

    private int countSukunaFingers() {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_WART) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void applyFingerEffects() {
        // ✅ Réinitialise les effets précédents
        player.removePotionEffect(PotionEffectType.SPEED);
        removePermanentHearts();

        double baseHealth = 20.0; // 10 cœurs de base
        double extraHearts = 0; // Variable pour stocker les cœurs supplémentaires

        // ✅ Chaque doigt donne 1 cœur permanent sauf le 3ème doigt qui donne Speed I
        for (int i = 1; i <= fingerCount; i++) {
            if (i == 3) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false)); // Speed I au 3ème doigt
            } else {
                extraHearts += 2; // 1 cœur = 2 points de vie
            }
        }

        // ✅ Appliquer Weakness aux fléaux si 5 doigts
        if (fingerCount >= 5) {
            applyWeaknessToFleauxNearby();
        }

        // ✅ Appliquer les cœurs supplémentaires
        double totalHealth = baseHealth + extraHearts;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(totalHealth);

        // ✅ Retirer Speed si le joueur repasse sous 3 doigts
        if (fingerCount < 3) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }

        player.sendMessage("§5☠️ Vous avez actuellement " + fingerCount + " doigt(s) de Sukuna !");
    }

    // ✅ Retirer les cœurs supplémentaires
    private void removePermanentHearts() {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0); // 10 cœurs de base
    }

    // ✅ Infliger Weakness aux fléaux proches
    private void applyWeaknessToFleauxNearby() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (fingerCount >= 5) {
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (!target.equals(player) && target.getLocation().distance(player.getLocation()) <= 10) {
                            // ✅ Appliquer Weakness si le joueur est un fléau
                            if (CampManager.getInstance().getCampOfRole(GameManager.getPlayerRole(target)) == CampType.FLEAUX) {
                                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0)); // Weakness pendant 5 secondes
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Vérifie toutes les 5 secondes
    }

    // ✅ Distribution de la Nether Star des Sorts Innés
    private void giveInnateSpellsItem() {
        ItemStack innateSpells = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = innateSpells.getItemMeta();
        meta.setDisplayName("§cSorts Innés de Sukuna");
        innateSpells.setItemMeta(meta);
        player.getInventory().addItem(innateSpells);
    }

    // ✅ Gestion des clics sur la Nether Star
    @EventHandler
    public void onInnateSpellsUse(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;
        if (!item.getItemMeta().getDisplayName().equals("§cSorts Innés de Sukuna")) return;

        Action action = event.getAction();

        // ✅ Priorité à l'extension si Shift + clic droit
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
            activateDomainExpansion(); // Active uniquement l'extension
            return; // ❌ Empêche d'autres capacités de s'activer
        }

        // ✅ Si pas en sneaky, activer les autres capacités
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            useAdvanceAndDamage();
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            useAbsorption();
        }
    }

    // ✅ Clic gauche : Avancer de 4 blocs et infliger des effets
    private void useAdvanceAndDamage() {
        if (innateLeftCooldown) {
            player.sendMessage("§cSort indisponible, temps de recharge actif !");
            return;
        }

        if (EnergyManager.getEnergy(player) < 300) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 300);

        // Avance de 4 blocs
        Location start = player.getLocation();
        @NotNull Vector direction = start.getDirection().normalize().multiply(4);
        Location end = start.add(direction);
        player.teleport(end);
        player.sendMessage("§c⚡ Vous vous êtes déplacé de 4 blocs avec puissance !");

        // Vérifie si un joueur est touché
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player) && target.getWorld().equals(player.getWorld())
                    && target.getLocation().distance(end) <= 1.5) {
                double healthReduction = 4.0; // -2 cœurs permanents
                target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(
                        target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() - healthReduction
                );
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 10)); // Immobilisation 0.5 sec
                target.sendMessage("§c💔 Vous avez été frappé par Sukuna, -2 cœurs temporaires !");

                // Restaurer les cœurs après 1 min 30 sec
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(
                                target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() + healthReduction
                        );
                        target.sendMessage("§a❤️ Vos cœurs perdus ont été restaurés !");
                    }
                }.runTaskLater(plugin, 1800L); // 1 min 30 sec (1800 ticks)
                break;
            }
        }

        // Début du cooldown de 4 minutes
        innateLeftCooldown = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                innateLeftCooldown = false;
                player.sendMessage("§a🌀 Votre sort gauche est prêt à être réutilisé !");
            }
        }.runTaskLater(plugin, 4800L); // 4 minutes
    }

    // ✅ Clic droit : 3 cœurs d'absorption pendant 4 secondes
    private void useAbsorption() {
        if (innateRightCooldown) {
            player.sendMessage("§cSort indisponible, temps de recharge actif !");
            return;
        }

        if (EnergyManager.getEnergy(player) < 300) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 300);
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 160, 1)); // 3 cœurs d'absorption

        player.sendMessage("§bVous bénéficiez de 3 cœurs d'absorption temporairement !");

        // Début du cooldown de 5 minutes
        innateRightCooldown = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                innateRightCooldown = false;
                player.sendMessage("§aVotre sort droit est prêt à être réutilisé !");
            }
        }.runTaskLater(plugin, 6000L); // 5 minutes
    }

    public static void initiateFingerSteal(Player sukuna, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            sukuna.sendMessage("§cLe joueur spécifié est introuvable ou hors-ligne.");
            return;
        }

        // ✅ Vérifie que Sukuna possède le rôle
        if (!GameManager.getPlayerRole(sukuna).equals(RoleType.SUKUNA)) {
            sukuna.sendMessage("§cVous devez être Sukuna pour utiliser cette commande !");
            return;
        }

        // ✅ Vérifie que le joueur ciblé possède un doigt
        boolean hasFinger = false;
        for (ItemStack item : target.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_WART) {
                hasFinger = true;
                break;
            }
        }

        if (!hasFinger) {
            sukuna.sendMessage("§cCe joueur ne possède pas de doigt de Sukuna !");
            return;
        }

        // ✅ Vérifie que Sukuna a assez d'énergie
        if (EnergyManager.getEnergy(sukuna) < 600) {
            sukuna.sendMessage("§cPas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(sukuna, 600);
        sukuna.sendMessage("§5☠️ Vous avez lancé le vol de doigt sur " + target.getName() + " !");

        // ✅ Crée une barre de boss pour afficher la progression
        BossBar stealProgressBar = Bukkit.createBossBar("§5Vol de doigt en cours...", BarColor.PURPLE, BarStyle.SOLID);
        stealProgressBar.addPlayer(sukuna);
        stealProgressBar.setProgress(0.0);

        final int totalTicks = 180 * 20; // 3 minutes
        final int[] ticksPassed = {0};

        // ✅ Lancer le vol avec une vérification régulière
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isDead() || !target.isOnline()) {
                    sukuna.sendMessage("§cLe vol a échoué : le joueur est mort ou déconnecté !");
                    stealProgressBar.removeAll();
                    stealActiveMap.remove(sukuna.getUniqueId());
                    this.cancel();
                    return;
                }

                boolean isClose = sukuna.getLocation().distance(target.getLocation()) <= 15;
                boolean wasActive = stealActiveMap.getOrDefault(sukuna.getUniqueId(), true);

                if (!isClose) {
                    if (wasActive) {
                        sukuna.sendMessage("§cVous êtes trop loin de " + target.getName() + " ! Le vol est interrompu.");
                        stealActiveMap.put(sukuna.getUniqueId(), false);
                    }
                    return;
                } else {
                    if (!wasActive) {
                        sukuna.sendMessage("§aVous êtes à nouveau proche de " + target.getName() + " ! Le vol reprend.");
                        stealActiveMap.put(sukuna.getUniqueId(), true);
                    }
                }

                ticksPassed[0] += 20;
                double progress = (double) ticksPassed[0] / totalTicks;
                stealProgressBar.setProgress(progress);

                if (ticksPassed[0] >= totalTicks) {
                    // ✅ Transfert d'1 seul doigt de Sukuna
                    for (ItemStack item : target.getInventory().getContents()) {
                        if (item != null && item.getType() == Material.NETHER_WART) {
                            item.setAmount(item.getAmount() - 1); // Retire 1 doigt à la cible

                            // Ajoute 1 doigt à Sukuna
                            sukuna.getInventory().addItem(new ItemStack(Material.NETHER_WART, 1));

                            sukuna.sendMessage("§5☠️ Vous avez volé 1 doigt à " + target.getName() + " !");
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (target.isOnline()) {
                                        target.sendMessage("§cVous remarquez qu’un doigt de Sukuna a disparu...");
                                    }
                                }
                            }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 20L * 60 * 5); // 5 minutes plus tard
                            break;
                        }
                    }

                    // ✅ Met à jour les effets des deux joueurs
                    updateSukunaFingerEffects(sukuna);

                    stealProgressBar.removeAll();
                    stealActiveMap.remove(sukuna.getUniqueId());
                    this.cancel();
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 20L);
    }

    // ✅ Mise à jour des effets de Sukuna en fonction des doigts volés
    private static void updateSukunaFingerEffects(Player sukuna) {
        int fingerCount = 0;
        for (ItemStack item : sukuna.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_WART) {
                fingerCount += item.getAmount();
            }
        }

        // ✅ Remise à zéro des effets
        sukuna.removePotionEffect(PotionEffectType.SPEED);
        sukuna.removePotionEffect(PotionEffectType.WEAKNESS);
        sukuna.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0); // 10 cœurs de base

        // ✅ Appliquer les effets en fonction du nombre de doigts
        if (fingerCount >= 3) {
            sukuna.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        } else {
            sukuna.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 + (fingerCount * 2)); // Chaque doigt = 1 cœur
        }

        if (fingerCount >= 5) {
            sukuna.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0));
        }

        sukuna.sendMessage("§5☠️ Vous possédez maintenant " + fingerCount + " doigt(s) !");
    }

    // ✅ Activation de l'Extension de Territoire
    private void activateDomainExpansion() {
        if (extensionCooldown) {
            player.sendMessage("§cL'extension est encore en cooldown !");
            return;
        }

        if (EnergyManager.getEnergy(player) < 1200) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 1200);

        World sukunaWorld = Bukkit.getWorld("Sukuna");
        if (sukunaWorld == null) {
            player.sendMessage("§cLe monde 'Sukuna' n'existe pas !");
            return;
        }

        Location spawnLocation = sukunaWorld.getSpawnLocation();
        List<Player> nearbyPlayers = getNearbyPlayers(10);
        teleportPlayersToDomain(nearbyPlayers, sukunaWorld, spawnLocation);

        player.sendMessage("§4Les joueurs ont été aspirés dans l'Hôtel Démoniaque !");

        // ✅ Lancer le cooldown de 10 minutes
        extensionCooldown = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                extensionCooldown = false;
                player.sendMessage("§aVotre Extension de Territoire est à nouveau disponible !");
            }
        }.runTaskLater(plugin, 12000L); // 10 minutes = 12000 ticks

        // ✅ Retour des joueurs au bout d'1 minute
        new BukkitRunnable() {
            @Override
            public void run() {
                returnPlayersToUHC(nearbyPlayers);
            }
        }.runTaskLater(plugin, 1200L); // 1 minute = 1200 ticks
    }

    // ✅ Obtenir les joueurs proches
    private List<Player> getNearbyPlayers(double radius) {
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player) && target.getWorld().equals(player.getWorld())
                    && target.getLocation().distance(player.getLocation()) <= radius) {
                nearbyPlayers.add(target);
            }
        }
        return nearbyPlayers;
    }

    // ✅ Téléportation dans l'extension
    private void teleportPlayersToDomain(List<Player> players, World domainWorld, Location spawn) {
        if (!players.contains(player)) {
            players.add(player);
        }

        for (Player target : players) {
            double randomX = spawn.getX() + (Math.random() * 30) - 15;
            double randomZ = spawn.getZ() + (Math.random() * 30) - 15;
            Location teleportLocation = new Location(domainWorld, randomX, 41, randomZ);
            target.teleport(teleportLocation);

            if (!target.equals(player)) {
                storeAndClearEffects(target); // ✅ Sauvegarde et suppression des effets
                target.sendMessage("§4Vous avez été aspiré dans l'Extension de Sukuna !");
            } else {
                player.sendMessage("§cVous êtes dans votre Hôtel Démoniaque !");
            }
        }
    }

    // ✅ Retour dans le monde UHC après 1 minute
    private void returnPlayersToUHC(List<Player> players) {
        World uhcWorld = Bukkit.getWorld("uhc");
        if (uhcWorld == null) {
            player.sendMessage("§cErreur : Monde UHC introuvable !");
            return;
        }

        double borderSize = uhcWorld.getWorldBorder().getSize();
        Location spawn = uhcWorld.getWorldBorder().getCenter();

        for (Player target : players) {
            double randomX = spawn.getX() + (Math.random() * borderSize / 2) - (borderSize / 4);
            double randomZ = spawn.getZ() + (Math.random() * borderSize / 2) - (borderSize / 4);
            int highestY = uhcWorld.getHighestBlockYAt((int) randomX, (int) randomZ) + 1;
            Location safeLocation = new Location(uhcWorld, randomX, highestY, randomZ);

            target.teleport(safeLocation);
            restorePlayerEffects(target);

            target.sendMessage("§aVous avez quitté l'extension de Sukuna !");
        }
    }

    // ✅ Activation avec Shift + Clic Droit sur la Nether Star
    @EventHandler
    public void onDomainExpansionActivate(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;
        if (!item.getItemMeta().getDisplayName().equals("§cSorts Innés de Sukuna")) return;

        Action action = event.getAction();

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
            activateDomainExpansion();
        }
    }

    // ✅ Stocker et enlever les effets avant la téléportation
    private void storeAndClearEffects(Player target) {
        savedEffects.put(target.getUniqueId(), new ArrayList<>(target.getActivePotionEffects()));
        target.getActivePotionEffects().forEach(effect -> target.removePotionEffect(effect.getType()));
    }

    // ✅ Restauration des effets des joueurs à leur retour
    private void restorePlayerEffects(Player target) {
        Collection<PotionEffect> effects = savedEffects.getOrDefault(target.getUniqueId(), Collections.emptyList());
        for (PotionEffect effect : effects) {
            target.addPotionEffect(effect);
        }
        savedEffects.remove(target.getUniqueId());
    }

    // ✅ Empêcher les autres joueurs de casser des blocs dans la dimension de Sukuna
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player target = event.getPlayer();
        World world = target.getWorld();

        // ✅ Autorise seulement Sukuna à casser des blocs
        if (world.getName().equals("Sukuna") && GameManager.getPlayerRole(target) != RoleType.SUKUNA) {
            event.setCancelled(true);
            target.sendMessage("§c❌ Vous ne pouvez pas casser de blocs !");
        }
    }

    // ✅ Empêcher les autres joueurs de poser des blocs dans la dimension de Sukuna
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player target = event.getPlayer();
        World world = target.getWorld();

        // ✅ Autorise seulement Sukuna à poser des blocs
        if (world.getName().equals("Sukuna") && GameManager.getPlayerRole(target) != RoleType.SUKUNA) {
            event.setCancelled(true);
            target.sendMessage("§c❌ Vous ne pouvez pas poser de blocs !");
        }
    }
}
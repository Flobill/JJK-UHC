package me.jjkuhc.jjkroles.exorcistes;

import me.jjkuhc.jjkgame.EnergyManager;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.attribute.Attribute;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.Sound;
import org.bukkit.event.block.Action;

import java.util.*;

public class Megumi implements Listener {
    private final Player player;
    private static final int MAX_ENERGIE_OCCULTE = 1000;
    private static final HashSet<UUID> elephantZonePlayers = new HashSet<>();

    //Extension
    private static final int EXTENSION_DURATION = 1200;
    private static final String EXTENSION_ITEM_NAME = "§5Jardin des Ombres";
    private static final int EXTENSION_COST = 850;
    private static final int MAX_PLAYERS = 5;
    private static final Map<UUID, Collection<PotionEffect>> savedEffects = new HashMap<>();


    private static final String FAMILIER_ITEM_NAME = "§aInvocateur de Shikigami";
    private static final String[] FAMILIERS = {"Chien de Jade", "Nue", "Éléphant de Plénitude", "Crapaud"};
    private static final HashMap<UUID, Integer> selectedFamiliar = new HashMap<>();
    private static final HashMap<UUID, Long> cooldownsChienDeJade = new HashMap<>();
    private static final HashMap<UUID, Long> cooldownsNue = new HashMap<>();
    private static final HashMap<UUID, Long> cooldownsCrapaud = new HashMap<>();


    private static final int COOLDOWN_TIME = 120; // Temps en secondes


    public Megumi(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            EnergyManager.setEnergy(player, 0);
            EnergyManager.setMaxEnergy(player, MAX_ENERGIE_OCCULTE);
            startNightBuffChecker();
            giveAbilityItem();
        }
    }

    // ✅ Réduction de 10% des dégâts subis par Sukuna
    @EventHandler
    public void onDamageTaken(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        if (GameManager.getPlayerRole(victim) == RoleType.MEGUMI && GameManager.getPlayerRole(attacker) == RoleType.SUKUNA) {
            event.setDamage(event.getDamage() * 0.9);
            attacker.sendMessage(ChatColor.RED + "Sukuna sent que Megumi est spécial...");
        }
    }

    // ✅ Effet Force 1 la nuit (mise à jour toutes les 10 secondes)
    private void startNightBuffChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (GameManager.getPlayerRole(p) == RoleType.MEGUMI) {
                        World world = p.getWorld();
                        long time = world.getTime();
                        if (time >= 13000 && time <= 23000) { // Nuit en ticks Minecraft
                            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 0, false, false));
                        } else {
                            p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                        }
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 200L); // Vérifie toutes les 10 sec
    }

    // ✅ Donner la Nether Star pour invoquer les familiers
    private void giveAbilityItem() {
        ItemStack abilityStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = abilityStar.getItemMeta();
        meta.setDisplayName(FAMILIER_ITEM_NAME);
        abilityStar.setItemMeta(meta);
        player.getInventory().addItem(abilityStar);

        ItemStack extensionStar = new ItemStack(Material.NETHER_STAR);
        meta.setDisplayName(EXTENSION_ITEM_NAME);
        extensionStar.setItemMeta(meta);
        player.getInventory().addItem(extensionStar);
    }

    // ✅ Gestion des clics pour sélectionner et invoquer les familiers
    @EventHandler
    public void onAbilityUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.NETHER_STAR || !item.hasItemMeta()) return;

        String itemName = item.getItemMeta().getDisplayName();

        // ✅ Vérifier si l'item est l'extension ou l'invocateur de familiers
        if (itemName.equals(EXTENSION_ITEM_NAME)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (p.isSneaking()) {
                    activateJardinDesOmbres();
                    event.setCancelled(true); // Annule l'interaction pour éviter les conflits
                    return;
                }
            }
        } else if (itemName.equals(FAMILIER_ITEM_NAME)) {
            event.setCancelled(true); // Annule l'interaction pour éviter de poser l'item

            UUID playerId = p.getUniqueId();
            if (!selectedFamiliar.containsKey(playerId)) {
                selectedFamiliar.put(playerId, 0); // Par défaut, sélectionne le premier familier
            }

            if (event.getAction().toString().contains("LEFT_CLICK")) {
                // ✅ Clic gauche : Changer de familier
                int nextFamiliar = (selectedFamiliar.get(playerId) + 1) % FAMILIERS.length;
                selectedFamiliar.put(playerId, nextFamiliar);
                p.sendMessage("§6🔮 Familier sélectionné : §b" + FAMILIERS[nextFamiliar]);

            } else if (event.getAction().toString().contains("RIGHT_CLICK")) {
                // ✅ Clic droit : Invoquer le familier sélectionné
                int selected = selectedFamiliar.get(playerId);
                p.sendMessage("§a🐾 Invocation de : §e" + FAMILIERS[selected]);

                switch (selected) {
                    case 0:
                        summonChienDeJade(p);
                        break;
                    case 1:
                        summonNue(p);
                        break;
                    case 2:
                        summonElephant(p);
                        break;
                    case 3:
                        summonCrapaud(p);
                        break;
                }
            }
        }
    }

    private void summonChienDeJade(Player player) {
        UUID playerId = player.getUniqueId();

        if (cooldownsChienDeJade.containsKey(playerId) && cooldownsChienDeJade.get(playerId) > System.currentTimeMillis()) {
            long remainingTime = (cooldownsChienDeJade.get(playerId) - System.currentTimeMillis()) / 1000;
            player.sendMessage("§c🐺 Chien de Jade est en cooldown ! (" + remainingTime + "s restantes)");
            return;
        }

        // ✅ Appliquer le cooldown
        cooldownsChienDeJade.put(playerId, System.currentTimeMillis() + (COOLDOWN_TIME * 1000));

        // Vérifier si le joueur a assez d’énergie occulte
        if (EnergyManager.getEnergy(player) < 200) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 200);
        player.sendMessage("§7🐺 Deux Chiens de Jade ont été invoqués !");

        // Récupérer la cible du joueur
        LivingEntity target = getNearestTarget(player, 20);
        if (target == null) {
            player.sendMessage("§e⚠ Pas de cible trouvée à proximité !");
            return;
        }

        for (int i = 0; i < 2; i++) {
            Wolf wolf = (Wolf) player.getWorld().spawn(player.getLocation(), Wolf.class);
            wolf.setCustomName("§6🐺 Chien de Jade");
            wolf.setCustomNameVisible(true);
            wolf.setOwner(player);
            wolf.setTamed(true);
            wolf.setAdult();
            wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0); // 20 cœurs
            wolf.setHealth(40.0);
            wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.4);
            wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(4.0); // Force 1 (2 cœurs)
            wolf.setTarget(target);
            wolf.setAngry(true);
            wolf.getWorld().playSound(wolf.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f);

            // Supprimer le loup après 30 secondes
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!wolf.isDead()) {
                        wolf.remove();
                        player.sendMessage("§7🐺 Un Chien de Jade a disparu...");
                    }
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 600L); // 30 sec (600 ticks)
        }
    }

    private LivingEntity getNearestTarget(Player player, double range) {
        LivingEntity nearest = null;
        double closestDistance = range;

        for (LivingEntity entity : player.getWorld().getLivingEntities()) {
            if (entity instanceof Player && !entity.equals(player)) {
                double distance = entity.getLocation().distance(player.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    nearest = entity;
                }
            }
        }
        return nearest;
    }

    private void summonNue(Player player) {
        UUID playerId = player.getUniqueId();

        if (cooldownsNue.containsKey(playerId) && cooldownsNue.get(playerId) > System.currentTimeMillis()) {
            long remainingTime = (cooldownsNue.get(playerId) - System.currentTimeMillis()) / 1000;
            player.sendMessage("§c🦅 Nue est en cooldown ! (" + remainingTime + "s restantes)");
            return;
        }

        cooldownsNue.put(playerId, System.currentTimeMillis() + (COOLDOWN_TIME * 1000));


        // Vérifier si le joueur a assez d’énergie occulte
        if (EnergyManager.getEnergy(player) < 100) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 100);
        player.sendMessage("§7🦅 Nue invoqué !");

        // Récupérer le bloc visé
        Block targetBlock = player.getTargetBlockExact(30);
        if (targetBlock == null) {
            player.sendMessage("§e⚠ Aucun bloc visé !");
            return;
        }

        Location center = targetBlock.getLocation();
        World world = center.getWorld();

        // ✅ Effet de particules rouges pour visualiser la zone
        new BukkitRunnable() {
            int duration = 10;

            @Override
            public void run() {
                if (duration <= 0) {
                    cancel();
                    return;
                }

                duration--;

                for (Player target : world.getPlayers()) {
                    if (target.equals(player)) continue; // ❌ Megumi ne prend pas de dégâts

                    if (target.getLocation().distance(center) <= 10) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Location feetLoc = target.getLocation().clone();
                                feetLoc.setY(feetLoc.getY() - 1);

                                boolean inWater = feetLoc.getBlock().getType() == Material.WATER
                                        || elephantZonePlayers.contains(target.getUniqueId()); // ✅ Nouveau : Vérifie si le joueur est dans la zone de l'Éléphant

                                double damage = inWater ? 2.0 : 1.0;

                                double newHealth = Math.max(0, target.getHealth() - damage);
                                target.setHealth(newHealth);

                                target.damage(0); // Animation de hit
                                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);

                                target.sendMessage("§c🔥 Nue vous inflige des dégâts !");
                            }
                        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 1L);
                    }
                }

                world.spawnParticle(Particle.REDSTONE, center, 100, 10, 1, 10, new Particle.DustOptions(Color.RED, 1.0f));
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 20L);
    }

    private void summonElephant(Player player) {
        if (EnergyManager.getEnergy(player) < 100) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 100);
        player.sendMessage("§7🐘 L’Éléphant de Plénitude est invoqué !");

        Block targetBlock = player.getTargetBlockExact(30);
        if (targetBlock == null) {
            player.sendMessage("§e⚠ Aucun bloc visé !");
            return;
        }

        Location center = targetBlock.getLocation();
        World world = center.getWorld();

        new BukkitRunnable() {
            int duration = 10; // Effet dure 10 secondes
            @Override
            public void run() {
                if (duration <= 0) {
                    cancel();
                    elephantZonePlayers.clear(); // ⚠ Nettoyer la liste après la fin de l'effet
                    return;
                }

                duration--;

                for (Player target : world.getPlayers()) {
                    if (target.getLocation().distance(center) <= 15) {
                        Location feetLocation = target.getLocation().clone();
                        feetLocation.setY(feetLocation.getY() - 1);

                        if (feetLocation.getBlock().getType() == Material.AIR) {
                            feetLocation.getBlock().setType(Material.WATER);
                        }

                        // ✅ Ajouter le joueur dans la liste
                        elephantZonePlayers.add(target.getUniqueId());

                        target.sendMessage("§b💦 Une vague d’eau envahit la zone !");
                    }
                }

                world.spawnParticle(Particle.WATER_SPLASH, center, 100, 15, 1, 15);
                world.playSound(center, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 20L);
    }

    private void summonCrapaud(Player player) {
        UUID playerId = player.getUniqueId();

        if (cooldownsCrapaud.containsKey(playerId) && cooldownsCrapaud.get(playerId) > System.currentTimeMillis()) {
            long remainingTime = (cooldownsCrapaud.get(playerId) - System.currentTimeMillis()) / 1000;
            player.sendMessage("§c🐸 Crapaud est en cooldown ! (" + remainingTime + "s restantes)");
            return;
        }

        cooldownsCrapaud.put(playerId, System.currentTimeMillis() + (COOLDOWN_TIME * 1000));

        // Vérifier si le joueur a assez d’énergie occulte
        if (EnergyManager.getEnergy(player) < 100) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 100);
        player.sendMessage("§7🐸 Le Crapaud est invoqué !");

        // Récupérer le bloc visé
        Block targetBlock = player.getTargetBlockExact(30);
        if (targetBlock == null) {
            player.sendMessage("§e⚠ Aucun bloc visé !");
            return;
        }

        Location center = targetBlock.getLocation();
        World world = center.getWorld();

        // ✅ Appliquer les effets et affichage des particules vertes
        new BukkitRunnable() {
            int duration = 10; // Effet dure 10 secondes
            @Override
            public void run() {
                if (duration <= 0) {
                    cancel();
                    return;
                }

                duration--;

                for (Player target : world.getPlayers()) {
                    if (target.equals(player)) continue; // ❌ Megumi ne prend pas d'effets

                    if (target.getLocation().distance(center) <= 5) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0)); // Slowness 1 (5s)
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 0));

                        target.sendMessage("§2🐸 Vous êtes ralenti par le Crapaud !");
                    }
                }

                // ✅ Affichage des particules vertes
                world.spawnParticle(Particle.VILLAGER_HAPPY, center, 100, 5, 1, 5);
                world.playSound(center, Sound.ENTITY_SLIME_SQUISH, 1.0f, 1.0f);
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 20L); // Tick toutes les secondes
    }

    // ✅ Activation du Jardin des Ombres
    private void activateJardinDesOmbres() {
        if (EnergyManager.getEnergy(player) < EXTENSION_COST) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte pour activer le Jardin des Ombres !");
            return;
        }

        EnergyManager.reduceEnergy(player, EXTENSION_COST);
        player.sendMessage("§5🌑 Vous avez activé le Jardin des Ombres !");

        World megumiWorld = Bukkit.getWorld("Megumi");
        if (megumiWorld == null) {
            player.sendMessage("§cErreur : Le monde 'Megumi' n'existe pas !");
            return;
        }

        Location spawnLocation = megumiWorld.getSpawnLocation();
        List<Player> nearbyPlayers = getNearbyPlayers(10);
        teleportPlayersToJardin(nearbyPlayers, megumiWorld, spawnLocation);

        new BukkitRunnable() {
            @Override
            public void run() {
                returnPlayersToUHC(nearbyPlayers);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), EXTENSION_DURATION);
    }

    // ✅ Liste des joueurs proches
    private List<Player> getNearbyPlayers(double radius) {
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player) && target.getWorld().equals(player.getWorld())
                    && target.getLocation().distance(player.getLocation()) <= radius) {
                nearbyPlayers.add(target);
            }
        }
        Collections.shuffle(nearbyPlayers);
        nearbyPlayers = nearbyPlayers.subList(0, Math.min(MAX_PLAYERS, nearbyPlayers.size()));
        nearbyPlayers.add(player); // Ajoute Megumi à la liste
        return nearbyPlayers;
    }

    // ✅ Téléportation avec suppression des effets avant d’entrer dans le Jardin
    private void teleportPlayersToJardin(List<Player> players, World megumiWorld, Location spawn) {
        for (Player target : players) {
            double randomX = spawn.getX() + (Math.random() * 30) - 15;
            double randomZ = spawn.getZ() + (Math.random() * 30) - 15;
            Location teleportLocation = new Location(megumiWorld, randomX, spawn.getY(), randomZ);
            target.teleport(teleportLocation);

            if (!target.equals(player)) {
                storeAndClearEffects(target); // ✅ Sauvegarde et suppression des effets
                target.removePotionEffect(PotionEffectType.INCREASE_DAMAGE); // ❌ Supprime immédiatement Force 1
                target.sendMessage("§c🌑 Vous êtes aspiré dans le Jardin des Ombres !");
            } else {
                player.sendMessage("§5🌑 Vous entrez dans votre Jardin !");
            }
        }

        // ✅ Lance une tâche pour surveiller et supprimer Force 1 en continu
        startEffectRemovalTask(megumiWorld);
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

            target.sendMessage("§aVous quittez le Jardin des Ombres !");
        }
    }

    // ✅ Empêcher les autres joueurs de casser des blocs dans la dimension de Megumi
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player target = event.getPlayer();
        World world = target.getWorld();

        if (world.getName().equals("Megumi") && !target.equals(player)) {
            event.setCancelled(true);
            target.sendMessage("§c❌ Vous ne pouvez pas casser de blocs ici !");
        }
    }

    // ✅ Empêcher les autres joueurs de poser des blocs dans la dimension de Megumi
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player target = event.getPlayer();
        World world = target.getWorld();

        if (world.getName().equals("Megumi") && !target.equals(player)) {
            event.setCancelled(true);
            target.sendMessage("§c❌ Vous ne pouvez pas poser de blocs ici !");
        }
    }

    // ✅ Stocker et enlever les effets avant la téléportation
    private void storeAndClearEffects(Player target) {
        savedEffects.put(target.getUniqueId(), new ArrayList<>(target.getActivePotionEffects()));

        // ✅ Supprime TOUS les effets avant la téléportation
        for (PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }

        // ❌ Supprime explicitement Force 1 pour Sukuna
        target.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
    }

    // ✅ Restaurer les effets des joueurs après leur retour
    private void restorePlayerEffects(Player target) {
        Collection<PotionEffect> effects = savedEffects.getOrDefault(target.getUniqueId(), Collections.emptyList());
        for (PotionEffect effect : effects) {
            target.addPotionEffect(effect);
        }
        savedEffects.remove(target.getUniqueId());
    }

    private void startEffectRemovalTask(World megumiWorld) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player target : megumiWorld.getPlayers()) {
                    if (!target.equals(player) && target.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                        target.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                        target.sendMessage("§c❌ Vos effets de Force ont été supprimés dans le Jardin des Ombres !");
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 100L); // Vérification toutes les 5 secondes
    }
}
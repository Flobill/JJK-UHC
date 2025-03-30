package me.jjkuhc.jjkroles.exorcistes;

import me.jjkuhc.jjkgame.EnergyManager;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkroles.CampType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Nobara implements Listener {
    private static final int CLOU_COST = 200;
    private static final int CLOU_DURATION_WARNING = 30; // 2 minutes
    private static final int CLOU_DURATION_DAMAGE = 60; // 10 minutes
    private static final int CLOU_DAMAGE = 4; // 2 cœurs = 4 HP
    private static final int CLOU_COOLDOWN = 600; // 10 minutes
    private final Player player;
    private static final int MAX_ENERGIE_OCCULTE = 600;
    private static final int EXPLOSION_CLOU_COST = 400;
    private BukkitTask actionBarTask;
    private boolean partageActif = false;
    private static final int PARTAGE_COUT = 400;

    private static final HashMap<UUID, UUID> clousMarques = new HashMap<>();
    private static final HashMap<UUID, Long> cooldownsClou = new HashMap<>();
    public static final Map<UUID, UUID> partageDouleurMap = new HashMap<>();


    public Nobara(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            EnergyManager.setEnergy(player, MAX_ENERGIE_OCCULTE);
            EnergyManager.setMaxEnergy(player, MAX_ENERGIE_OCCULTE);
            donnerPochetteDeClous();
            donnerClousDepart();
        }
    }

    public static void poserClou(Player nobara, Player cible) {
        UUID nobaraID = nobara.getUniqueId();
        UUID cibleID = cible.getUniqueId();

        // ✅ Vérifie si Nobara a assez d’énergie occulte
        if (EnergyManager.getEnergy(nobara) < CLOU_COST) {
            nobara.sendMessage(ChatColor.RED + "❌ Pas assez d'énergie occulte !");
            return;
        }

        // ✅ Vérifie si un cooldown est actif
        if (cooldownsClou.containsKey(nobaraID) && cooldownsClou.get(nobaraID) > System.currentTimeMillis()) {
            long remainingTime = (cooldownsClou.get(nobaraID) - System.currentTimeMillis()) / 1000;
            nobara.sendMessage(ChatColor.RED + "Vous devez attendre encore " + remainingTime + "s avant de poser un autre clou !");
            return;
        }

        // ✅ Vérifie si la cible a déjà un clou
        if (clousMarques.containsValue(cibleID)) {
            nobara.sendMessage(ChatColor.RED + "⚠ Ce joueur a déjà un clou posé !");
            return;
        }

        // ✅ Applique le coût et pose le clou
        if (!retirerClou(nobara)) {
            nobara.sendMessage(ChatColor.RED + "❌ Vous n'avez plus de clous !");
            return;
        }

        clousMarques.put(nobaraID, cibleID);
        cooldownsClou.put(nobaraID, System.currentTimeMillis() + (CLOU_COOLDOWN * 1000));

        nobara.sendMessage(ChatColor.GOLD + "Vous avez posé un clou sur " + cible.getName() + " !");

        // ✅ Notification après 2 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (clousMarques.containsKey(nobaraID) && clousMarques.get(nobaraID).equals(cibleID)) {
                    cible.sendMessage(ChatColor.RED + "⚠ Vous ressentez une étrange présence... Un clou maudit est peut-être sur vous !");
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), CLOU_DURATION_WARNING * 20L);

        // ✅ Dégâts après 10 minutes + révélation du camp
        new BukkitRunnable() {
            @Override
            public void run() {
                if (clousMarques.containsKey(nobaraID) && clousMarques.get(nobaraID).equals(cibleID)) {
                    double newHealth = Math.max(0, cible.getHealth() - CLOU_DAMAGE);
                    cible.setHealth(newHealth);

                    cible.sendMessage(ChatColor.DARK_RED + "💥 Un clou maudit vous transperce, infligeant 2 cœurs de dégâts !");

                    CampType camp = GameManager.getPlayerCamp(cible);
                    if (camp.equals(CampType.FLEAUX)) {
                        nobara.sendMessage(ChatColor.DARK_PURPLE + "⚠ " + cible.getName() + " est un fléau !");
                        // ✅ Avertir la cible SEULEMENT si c’est un Fléau
                        cible.sendMessage(ChatColor.DARK_RED + "⚠ + nobara.getName() +  a découvert votre camp !");
                    } else {
                        nobara.sendMessage(ChatColor.GREEN + "✔ " + cible.getName() + " n'est PAS un fléau.");
                    }
                    clousMarques.remove(nobaraID);
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), CLOU_DURATION_DAMAGE * 20L);

        // ✅ Suppression du clou après 15 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                clousMarques.remove(nobaraID);
                nobara.sendMessage(ChatColor.GRAY + "Le clou posé sur " + cible.getName() + " a disparu.");
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), 900 * 20L); // 15 minutes
    }

    private void donnerPochetteDeClous() {
        // ✅ Vérifier si Nobara a déjà la Nether Star
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_STAR &&
                    item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "🧶 Pochette de Clous")) {
                return; // ✅ Nobara a déjà l'item, on ne le redonne pas
            }
        }

        // ✅ Donner l’item seulement si elle ne l’a pas
        ItemStack pochetteClous = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = pochetteClous.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "🧶 Pochette de Clous");
        pochetteClous.setItemMeta(meta);
        player.getInventory().addItem(pochetteClous);
    }

    private void exploserClou(Player nobara) {
        UUID nobaraID = nobara.getUniqueId();

        // ✅ Vérifier si Nobara a un clou posé
        if (!clousMarques.containsKey(nobaraID)) {
            nobara.sendMessage(ChatColor.RED + "❌ Vous n'avez aucun clou posé !");
            return;
        }

        // ✅ Vérifier si Nobara a assez d’énergie occulte
        if (EnergyManager.getEnergy(nobara) < EXPLOSION_CLOU_COST) {
            nobara.sendMessage(ChatColor.RED + "❌ Pas assez d'énergie occulte !");
            return;
        }

        // ✅ Retirer le clou et récupérer la cible
        UUID cibleID = clousMarques.get(nobaraID);
        Player cible = Bukkit.getPlayer(cibleID);

        if (cible == null) {
            nobara.sendMessage(ChatColor.RED + "⚠ La cible du clou n'est plus en ligne !");
            return;
        }

        clousMarques.remove(nobaraID);
        EnergyManager.reduceEnergy(nobara, EXPLOSION_CLOU_COST);

        // ✅ Appliquer les dégâts bruts (ignorer l’armure)
        double newHealth = Math.max(0, cible.getHealth() - 6); // 3 cœurs bruts
        cible.setHealth(newHealth);

        // ✅ Nobara gagne 5 minutes de Résistance 1
        nobara.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 0)); // 5 minutes = 6000 ticks

        // ✅ Effet sonore et visuel
        cible.getWorld().playSound(cible.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        cible.sendMessage(ChatColor.DARK_RED + "💥 Un clou maudit explose sur vous, infligeant 3 cœurs !");
        nobara.sendMessage(ChatColor.GOLD + "🧶 Vous avez explosé un clou !");
    }

    @EventHandler
    public void onClouInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.NETHER_STAR || !item.hasItemMeta()) return;

        String itemName = item.getItemMeta().getDisplayName();

        // ✅ Vérifier si c'est la Pochette de Clous
        if (!itemName.equals(ChatColor.GOLD + "🧶 Pochette de Clous")) return;

        event.setCancelled(true); // Annule toute autre interaction avec l’item

        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            exploserClou(player); // ✅ Explosion du clou (Clic droit)
        } else if (event.getAction().toString().contains("LEFT_CLICK")) {
            traquerClou(player); // ✅ Traque du joueur marqué (Clic gauche)
        }
    }

    private void afficherActionBarTraque(Player nobara, Player cible) {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }

        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!cible.isOnline() || !clousMarques.containsKey(nobara.getUniqueId())) {
                    cancel();
                    return;
                }

                double distance = nobara.getLocation().distance(cible.getLocation());
                String direction = getDirection(nobara.getLocation(), cible.getLocation());

                // ✅ Suppression de caractères invisibles et normalisation du texte
                String message = ChatColor.YELLOW.toString() + direction.trim() + " " +
                        ChatColor.RED.toString() + (int) distance + " blocs";

                nobara.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("JJKUHC"), 0L, 40L);
    }

    private String getDirection(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double angle = Math.atan2(direction.getZ(), direction.getX());

        // ✅ Ajustement selon la direction où Nobara regarde
        double playerYaw = from.getYaw() + 90; // 🔄 Correction du décalage de 90°
        double relativeAngle = Math.toDegrees(angle) - playerYaw;

        // ✅ Normalisation de l'angle pour qu'il soit entre 0 et 360
        if (relativeAngle < 0) relativeAngle += 360;
        if (relativeAngle >= 360) relativeAngle -= 360;

        // ✅ Affectation des flèches en fonction de la perspective de Nobara
        if (relativeAngle >= 337.5 || relativeAngle < 22.5) return "⬆️"; // Devant
        if (relativeAngle >= 22.5 && relativeAngle < 67.5) return "↗️"; // Devant-Droite
        if (relativeAngle >= 67.5 && relativeAngle < 112.5) return "➡️"; // Droite
        if (relativeAngle >= 112.5 && relativeAngle < 157.5) return "↘️"; // Derrière-Droite
        if (relativeAngle >= 157.5 && relativeAngle < 202.5) return "⬇️"; // Derrière
        if (relativeAngle >= 202.5 && relativeAngle < 247.5) return "↙️"; // Derrière-Gauche
        if (relativeAngle >= 247.5 && relativeAngle < 292.5) return "⬅️"; // Gauche
        return "↖️";
    }

    private void traquerClou(Player nobara) {
        UUID nobaraID = nobara.getUniqueId();

        if (!clousMarques.containsKey(nobaraID)) {
            nobara.sendMessage(ChatColor.RED + "❌ Vous n'avez aucun clou posé !");
            return;
        }

        UUID cibleID = clousMarques.get(nobaraID);
        Player cible = Bukkit.getPlayer(cibleID);

        if (cible == null) {
            nobara.sendMessage(ChatColor.RED + "⚠ La cible du clou n'est plus en ligne !");
            return;
        }

        // ✅ Activer l'affichage dynamique de l’Action Bar
        afficherActionBarTraque(nobara, cible);

        // ✅ Message de confirmation (sans boussole)
        nobara.sendMessage(ChatColor.YELLOW + "🧭 Vous suivez maintenant " + ChatColor.GOLD + cible.getName() +
                ChatColor.YELLOW + ". Distance : " + ChatColor.RED + (int) nobara.getLocation().distance(cible.getLocation()) + " blocs.");
    }

    public void activerPartageDouleurs(Player nobara, Player cible) {
        if (partageActif) {
            nobara.sendMessage(ChatColor.RED + "❌ Vous avez déjà activé le partage des douleurs !");
            return;
        }

        if (EnergyManager.getEnergy(nobara) < PARTAGE_COUT) {
            nobara.sendMessage(ChatColor.RED + "❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(nobara, PARTAGE_COUT);
        partageActif = true;
        partageDouleurMap.put(nobara.getUniqueId(), cible.getUniqueId());
        nobara.setMaxHealth(12);
        nobara.sendMessage(ChatColor.AQUA + "Partage de vie activé avec : " + cible.getName());
    }

    @EventHandler
    public void onNobaraDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player nobara = (Player) event.getEntity();
        UUID nobaraID = nobara.getUniqueId();

        if (!partageDouleurMap.containsKey(nobaraID)) return;

        Player cible = Bukkit.getPlayer(partageDouleurMap.get(nobaraID));
        if (cible == null || !cible.isOnline()) {
            partageDouleurMap.remove(nobaraID);
            return;
        }

        double damage = event.getFinalDamage();
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
                if (cible.isOnline()) {
                    double cibleHealth = cible.getHealth();

                    double maxDamage = cibleHealth - 1;

                    if (maxDamage > 0) {
                        double finalDamage = Math.min(damage, maxDamage);
                        cible.damage(finalDamage);
                        cible.sendMessage(ChatColor.RED + "💀 Vous ressentez la douleur de Nobara !");
                    }
                }
            });
        });
    }

    @EventHandler
    public void onNobaraDeath(PlayerDeathEvent event) {
        Player nobara = event.getEntity();
        UUID nobaraID = nobara.getUniqueId();

        if (!partageDouleurMap.containsKey(nobaraID)) {
            return;
        }

        Player cible = Bukkit.getPlayer(partageDouleurMap.get(nobaraID));
        if (cible != null && cible.isOnline()) {
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("JJKUHC"), () -> {
                if (cible.getHealth() > 4) {
                    cible.damage(4);
                } else {
                    cible.setHealth(1);
                }
                cible.sendMessage(ChatColor.DARK_RED + "💔 Nobara est morte, vous perdez 2 cœurs !");
            });
        }
        partageDouleurMap.remove(nobaraID);
    }

    private static boolean retirerClou(Player nobara) {
        ItemStack[] contents = nobara.getInventory().getContents();

        for (ItemStack item : contents) {
            if (item != null && item.getType() == Material.IRON_NUGGET) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    nobara.getInventory().remove(item);
                }
                return true; // ✅ Clou retiré avec succès
            }
        }
        return false; // ❌ Aucun clou à retirer
    }

    private void donnerClousDepart() {
        int clousExistants = 0;

        // Vérifier combien de clous Nobara possède déjà
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.IRON_NUGGET) {
                clousExistants += item.getAmount();
            }
        }

        // Si elle n'a pas encore ses 4 clous, lui en donner
        if (clousExistants < 4) {
            ItemStack clous = new ItemStack(Material.IRON_NUGGET, 4 - clousExistants);
            ItemMeta meta = clous.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "🧶 Clou Maudit");
            clous.setItemMeta(meta);
            player.getInventory().addItem(clous);
        }
    }

    private void ajouterClou(Player nobara) {
        ItemStack clou = new ItemStack(Material.IRON_NUGGET, 1);
        ItemMeta meta = clou.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "🧶 Clou Maudit");
        clou.setItemMeta(meta);
        nobara.getInventory().addItem(clou);
    }
}
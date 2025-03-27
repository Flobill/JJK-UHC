package me.jjkuhc.jjkroles.exorcistes;

import me.jjkuhc.Main;
import me.jjkuhc.jjkconfig.PacteMenu;
import me.jjkuhc.jjkgame.EnergyManager;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.block.Action;
import org.bukkit.attribute.Attribute;

public class Itadori implements Listener {
    private final Player player;
    private boolean sprintActive = false;
    private BukkitRunnable sprintTask;
    private boolean hasBlackFlashActive = false;
    private static final int MAX_ENERGIE_OCCULTE = 800;

    public Itadori(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            applyPermanentEffects();
            EnergyManager.setEnergy(player, MAX_ENERGIE_OCCULTE);
            EnergyManager.setMaxEnergy(player, MAX_ENERGIE_OCCULTE);
            giveAbilityItem();
        }
    }

    // ✅ Effet Force I permanent
    private void applyPermanentEffects() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false));
    }

    // ✅ Donne la Nether Star pour ses capacités
    private void giveAbilityItem() {
        ItemStack abilityStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = abilityStar.getItemMeta();
        meta.setDisplayName("§dPouvoirs de Yuji Itadori");
        abilityStar.setItemMeta(meta);
        player.getInventory().addItem(abilityStar);
    }

    // ✅ Redonner Force I si jamais il la perd
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().equals(player)) {
            applyPermanentEffects();
        }
    }

    // ✅ Gestion des clics sur la Nether Star
    @EventHandler
    public void onAbilityUse(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player)) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;
        if (!item.getItemMeta().getDisplayName().equals("§dPouvoirs de Yuji Itadori")) return;

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            toggleSprint();
        }
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            activateBlackFlash();
        }
    }

    // ✅ Active/Désactive Sprint
    private void toggleSprint() {
        if (sprintActive) {
            disableSprint();
        } else {
            enableSprint();
        }
    }

    private void enableSprint() {
        if (EnergyManager.getEnergy(player) < 5) {
            player.sendMessage("§c❌ Pas assez d'énergie pour utiliser Sprint !");
            return;
        }

        sprintActive = true;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        player.sendMessage("§aSprint activé ! Vous perdez 5 énergie/s.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1.0f, 1.2f);

        sprintTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (EnergyManager.getEnergy(player) < 5) {
                    disableSprint();
                    return;
                }
                EnergyManager.reduceEnergy(player, 5);
            }
        };
        sprintTask.runTaskTimer(JavaPlugin.getPlugin(Main.class), 20L, 20L);
    }

    private void disableSprint() {
        if (!sprintActive) return;

        sprintActive = false;
        player.removePotionEffect(PotionEffectType.SPEED);
        player.sendMessage("§cSprint désactivé !");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1.0f, 0.8f);

        if (sprintTask != null) {
            sprintTask.cancel();
        }
    }

    private void activateBlackFlash() {
        if (hasBlackFlashActive) {
            player.sendMessage("§c⚡ Vous avez déjà activé Éclair Noir, utilisez-le avant d’en activer un autre !");
            return;
        }

        if (EnergyManager.getEnergy(player) < 300) {
            player.sendMessage("§c❌ Pas assez d'énergie occulte !");
            return;
        }

        EnergyManager.reduceEnergy(player, 300);
        hasBlackFlashActive = true;

        player.sendMessage("§6⚡ Éclair Noir activé ! Votre prochain coup fera +1 cœur de dégâts.");
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return; // ✅ Vérifie que la cible est un joueur

        Player attacker = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        // ✅ Vérifie que c'est bien Itadori qui attaque
        if (!attacker.equals(player)) return;
        if (!hasBlackFlashActive) return;

        // ✅ Vérifie qu'Itadori utilise bien une épée
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon.getType().toString().contains("SWORD")) {
            double baseDamage = event.getDamage();
            double bonusDamage = 4.0; // ✅ +1 cœur (2 points de dégâts)
            double newDamage = baseDamage + bonusDamage * 1.5;

            event.setDamage(newDamage);

            // ✅ Messages de debug
            attacker.sendMessage("§6💥 Éclair Noir déclenché ! Dégâts bonus appliqués.");
            target.sendMessage("§c⚡ Vous avez été touché par un Éclair Noir !");

            // ✅ Désactive Éclair Noir après le coup
            hasBlackFlashActive = false;
        } else {
            attacker.sendMessage("§c⚠ Éclair Noir ne peut être activé qu'avec une épée !");
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        // ✅ Vérifie qu'il utilise bien une épée
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (!weapon.getType().name().contains("SWORD")) return;

        // ✅ Passe `target` en paramètre pour gérer les rôles séparément
        EnergyManager.handleEnergyGain(attacker, target, event.isCritical());
    }

    public static void checkAndApplyRegeneration(Player player) {
        if (!GameManager.getPlayerRole(player).equals(RoleType.ITADORI)) return;

        int sukunaFingers = countSukunaFingers(player);

        if (sukunaFingers > 0 && player.getHealth() <= 16) { // Moins de 4 cœurs (8 points de vie)
            double newHealth = Math.min(player.getHealth() + 4, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            player.setHealth(newHealth);
            player.sendMessage("§a💖 Grâce au pouvoir de Sukuna, vous régénérez §c2 cœurs !");
        }
    }

    // ✅ Compte les doigts de Sukuna dans l'inventaire d'Itadori
    private static int countSukunaFingers(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_WART) {
                count += item.getAmount();
            }
        }
        return count;
    }

    @EventHandler
    public void onAttackSukuna(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        // Vérifie si l'attaquant est Itadori et a choisi le pacte d'Ignorance
        if (GameManager.getPlayerRole(attacker) == RoleType.ITADORI
                && PacteMenu.getPacte(attacker).equals("Ignorance")
                && GameManager.getPlayerRole(target) == RoleType.SUKUNA) {

            // Applique +10% de dégâts contre Sukuna
            double damage = event.getDamage();
            double increasedDamage = damage * 1.1; // +10%
            event.setDamage(increasedDamage);
        }
    }
}
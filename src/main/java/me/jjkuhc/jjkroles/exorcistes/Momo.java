package me.jjkuhc.jjkroles.exorcistes;

import me.jjkuhc.jjkgame.EnergyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class Momo implements Listener {

    private static final int MAX_ENERGIE_OCCULTE = 600;
    private final Player player;
    private boolean furtiviteActive = false;
    private static final int FURTIVITE_COST = 1; // 1 énergie par seconde
    private boolean balaiUtilise = false;
    private static final int BALAI_COST = 500;
    private static final int DETECTION_RAYON = 20; // Portée de détection des fléaux
    private static final Map<UUID, Momo> momoInstances = new HashMap<>();

    public Momo(Player player) {
        this.player = player;
        if (player != null && player.isOnline()) {
            EnergyManager.setEnergy(player, MAX_ENERGIE_OCCULTE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            momoInstances.put(player.getUniqueId(), this);
        }
    }

    public static Momo getMomoInstance(Player player) {
        return momoInstances.get(player.getUniqueId());
    }

    // ✅ Activation Furtivité (à coder ensuite)
    public void activerFurtivite() {}

    // ✅ Activation Balai de Paille (à coder ensuite)
    public void activerBalai() {}

    // ✅ Détection des Fléaux en début d'épisode (à coder ensuite)
    public static void detecterFleauxDebutEpisode() {}
}
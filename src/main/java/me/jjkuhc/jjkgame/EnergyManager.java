package me.jjkuhc.jjkgame;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class EnergyManager {
    private static final HashMap<UUID, Integer> energyMap = new HashMap<>();
    private static final int DEFAULT_ENERGY = 200; // Valeur par défaut si non spécifié

    // Initialisation de l'énergie pour un joueur
    public static void setEnergy(Player player, int amount) {
        energyMap.put(player.getUniqueId(), amount);
    }

    // Obtenir l'énergie actuelle du joueur
    public static int getEnergy(Player player) {
        return energyMap.getOrDefault(player.getUniqueId(), DEFAULT_ENERGY);
    }

    // Augmenter l'énergie
    public static void addEnergy(Player player, int amount) {
        setEnergy(player, getEnergy(player) + amount);
    }

    // Diminuer l'énergie
    public static void reduceEnergy(Player player, int amount) {
        setEnergy(player, Math.max(0, getEnergy(player) - amount)); // Empêche l'énergie d'être négative
    }

    // Réinitialiser l'énergie pour un joueur
    public static void resetEnergy(Player player) {
        setEnergy(player, DEFAULT_ENERGY);
    }
}
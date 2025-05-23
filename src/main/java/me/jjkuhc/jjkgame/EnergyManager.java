package me.jjkuhc.jjkgame;

import me.jjkuhc.jjkroles.RoleType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.HashMap;
import java.util.UUID;

public class EnergyManager implements CommandExecutor {
    private static final HashMap<UUID, Integer> energyMap = new HashMap<>();
    private static final HashMap<UUID, Integer> maxEnergyMap = new HashMap<>(); // Ajout de l'énergie max
    private static final HashMap<UUID, Long> blockedPlayers = new HashMap<>(); // Gestion du blocage des capacités
    private static final int DEFAULT_ENERGY = 200;

    // Initialisation de l'énergie pour un joueur
    public static void setEnergy(Player player, int amount) {
        energyMap.put(player.getUniqueId(), amount);
    }

    // Définir l'énergie maximale d'un joueur
    public static void setMaxEnergy(Player player, int amount) {
        maxEnergyMap.put(player.getUniqueId(), amount);
    }

    // Obtenir l'énergie actuelle du joueur
    public static int getEnergy(Player player) {
        return energyMap.getOrDefault(player.getUniqueId(), DEFAULT_ENERGY);
    }

    // Obtenir l'énergie maximale du joueur
    public static int getMaxEnergy(Player player) {
        return maxEnergyMap.getOrDefault(player.getUniqueId(), DEFAULT_ENERGY);
    }

    // Augmenter l'énergie
    public static void addEnergy(Player player, int amount) {
        setEnergy(player, getEnergy(player) + amount);
    }

    // Diminuer l'énergie
    public static void reduceEnergy(Player player, int amount) {
        setEnergy(player, Math.max(0, getEnergy(player) - amount));
    }

    // ✅ Commande /giveenergy <joueur> <quantité>
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§c❌ Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§c❌ Utilisation : /giveenergy <joueur> <quantité>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§c❌ Joueur introuvable ou hors-ligne !");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                sender.sendMessage("§c❌ La quantité d'énergie doit être supérieure à 0 !");
                return true;
            }

            addEnergy(target, amount);
            sender.sendMessage("§a✔ Vous avez ajouté §b" + amount + "§a points d'énergie occulte à §b" + target.getName() + "§a !");
            target.sendMessage("§b⚡ Vous avez reçu §e" + amount + "§b points d'énergie occulte !");
        } catch (NumberFormatException e) {
            sender.sendMessage("§c❌ Veuillez entrer un nombre valide !");
        }

        return true;
    }

    public static void handleEnergyGain(Player attacker, Player target, boolean isCritical) {
        int baseGain = 4;
        int energyGain = baseGain;

        if (GameManager.getPlayerRole(attacker) == RoleType.ITADORI && isCritical) {
            energyGain *= 2;
        }

        if (GameManager.getPlayerRole(attacker) == RoleType.ITADORI) {
            int currentEnergy = getEnergy(attacker);
            if (currentEnergy + energyGain > 800) {
                energyGain = 800 - currentEnergy;
            }
        }

        if (energyGain > 0) {
            addEnergy(attacker, energyGain);
        }
    }

    public static void startPassiveRegen(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int current = getEnergy(player);
                    int max = getMaxEnergy(player);
                    if (current < max) {
                        setEnergy(player, Math.min(current + 1, max));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // toutes les secondes
    }
}
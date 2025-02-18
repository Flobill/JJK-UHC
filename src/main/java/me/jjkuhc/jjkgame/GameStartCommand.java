package me.jjkuhc.jjkgame;

import me.jjkuhc.host.HostManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import me.jjkuhc.scoreboard.ScoreboardManager;



import java.util.Random;

public class GameStartCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ScoreboardManager scoreboardManager;

    public GameStartCommand(JavaPlugin plugin, me.jjkuhc.scoreboard.ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande !");
            return true;
        }

        Player player = (Player) sender;

        // Vérifie si l'host est bien celui qui lance la partie
        if (!HostManager.isHost(player)) {
            player.sendMessage("§cSeul l'host peut démarrer la partie !");
            return true;
        }

        // Vérifie que la partie est en attente
        if (!GameManager.isState(GameState.EN_ATTENTE)) {
            sender.sendMessage("§cLa partie ne peut être lancée que lorsqu'elle est en attente !");
            return true;
        }

        // Vérifie s'il y a au moins 2 joueurs
        if (Bukkit.getOnlinePlayers().size() < 2) {
            sender.sendMessage("§cIl faut au moins 2 joueurs pour commencer la partie !");
            return true;
        }

        // Lancer le compte à rebours
        startCountdown();
        return true;
    }

    private void startCountdown() {
        GameManager.setCurrentState(GameState.EN_LANCEMENT);
        scoreboardManager.updateAllScoreboards();

        Bukkit.broadcastMessage("§6 La partie commence dans 30 secondes !");

        new BukkitRunnable() {
            int countdown = 30;

            @Override
            public void run() {
                if (countdown == 30 || countdown == 10 || countdown <= 5) {
                    Bukkit.broadcastMessage("§e⏳ Début dans §c" + countdown + "s !");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 10);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                    }
                }

                if (countdown <= 3) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 30);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    }
                }

                if (countdown <= 0) {
                    this.cancel();
                    Bukkit.broadcastMessage("§a🚀 Début de la partie !");
                    Bukkit.getLogger().info("[DEBUG] Début de la partie - Téléportation en cours...");

                    GameManager.setCurrentState(GameState.EN_COURS);
                    scoreboardManager.updateAllScoreboards();

                    // ✅ Téléportation d'abord
                    teleportPlayers();

                    // ✅ Jouer les sons et particules APRÈS la téléportation (délai 5 ticks = 0.25 sec)
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation().add(0, 1, 0), 1);
                                player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 50);
                                player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation().add(0, 2, 0), 30);

                                // 🔥 Cri du dragon (Java) et cri du Wither (Bedrock)
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 10.0f, 1.0f);
                                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 10.0f, 1.0f);
                            }
                            Bukkit.getLogger().info("[DEBUG] Sons et particules joués après la téléportation !");
                        }
                    }.runTaskLater(plugin, 5L); // 5 ticks = 0.25 sec après la TP

                    return;
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }


    private void teleportPlayers() {
        World uhcWorld = Bukkit.getWorld("uhc"); // Assurez-vous que le monde est bien chargé
        if (uhcWorld == null) {
            Bukkit.broadcastMessage("§c❌ Erreur : Le monde UHC n'est pas chargé !");
            return;
        }

        int borderSize = (int) uhcWorld.getWorldBorder().getSize() / 2;
        Location spawnCenter = uhcWorld.getSpawnLocation();
        Random random = new Random();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location randomLocation = getRandomLocation(uhcWorld, spawnCenter, borderSize);
            player.teleport(randomLocation);
            player.sendMessage("§b📍 Vous avez été téléporté !");
        }

        // Changer l'état de la partie à "En cours"
        GameManager.setCurrentState(GameState.EN_COURS);
    }

    private Location getRandomLocation(World world, Location center, int radius) {
        Random random = new Random();
        Location loc;

        do {
            int x = center.getBlockX() + random.nextInt(radius * 2) - radius;
            int z = center.getBlockZ() + random.nextInt(radius * 2) - radius;
            int y = world.getHighestBlockYAt(x, z);

            loc = new Location(world, x + 0.5, y + 1, z + 0.5);
        } while (isDangerousBlock(loc));

        return loc;
    }

    private boolean isDangerousBlock(Location loc) {
        Material blockType = loc.getBlock().getType();
        return blockType == Material.WATER || blockType == Material.LAVA || blockType == Material.CACTUS;
    }
}
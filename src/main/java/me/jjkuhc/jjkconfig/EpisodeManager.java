package me.jjkuhc.jjkconfig;

import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameState;
import me.jjkuhc.jjkroles.exorcistes.Momo;
import me.jjkuhc.jjkroles.fleaux.Hanami;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;


public class EpisodeManager {

    private static boolean isDay = true;
    private static int episodeCount = 0;
    public static boolean isDay() { return isDay; }


    public static void startEpisodeCycle() {
        if (!GameManager.isState(GameState.EN_COURS)) return;
        runDayPhase();
    }

    private static void runDayPhase() {
        isDay = true;
        episodeCount++;
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§e☀ Jour " + episodeCount + " commence !");
        Bukkit.broadcastMessage("");
        GameManager.handleEpisodeStart();
        Momo.momoInstances.values().forEach(Momo::envoyerResultatDetection);
        Hanami.verifierToutesLesMalédictions();


        // ✅ On force midi pile au début de la journée
        World uhcWorld = Bukkit.getWorld("UHC");
        if (uhcWorld != null) {
            uhcWorld.setTime(6000); // 6000 = midi pile
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                runNightPhase();
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), TimerConfigMenu.getDayDuration() * 20L);
    }

    private static void runNightPhase() {
        isDay = false;
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8La nuit tombe...");
        Bukkit.broadcastMessage("");
        Momo.momoInstances.values().forEach(Momo::startDetectionNuit);

        // ✅ On force minuit pile au début de la nuit
        World uhcWorld = Bukkit.getWorld("UHC");
        if (uhcWorld != null) {
            uhcWorld.setTime(18000); // 18000 = minuit pile
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                runDayPhase();
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("JJKUHC"), TimerConfigMenu.getNightDuration() * 20L);
    }

    public static int getEpisodeCount() {
        return episodeCount;
    }

}
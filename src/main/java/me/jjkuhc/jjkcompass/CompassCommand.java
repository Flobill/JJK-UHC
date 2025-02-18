package me.jjkuhc.jjkcompass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassCommand implements CommandExecutor {
    private JavaPlugin plugin;

    public CompassCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande !");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("spawn")) {
            if (!plugin.getConfig().contains("spawn")) {
                player.sendMessage("§cLe spawn n'a pas été défini. Utilisez /setspawn.");
                return true;
            }
            Location spawn = getLocationFromConfig("spawn");
            player.teleport(spawn);
            player.sendMessage("§aTéléportation au spawn !");
        }

        if (cmd.equals("jump")) {
            if (!plugin.getConfig().contains("jump")) {
                player.sendMessage("§cLe jump n'a pas été défini. Utilisez /setjump.");
                return true;
            }
            Location jump = getLocationFromConfig("jump");
            player.teleport(jump);
            player.sendMessage("§aTéléportation au jump !");
        }

        return true;
    }

    private Location getLocationFromConfig(String path) {
        double x = plugin.getConfig().getDouble(path + ".x");
        double y = plugin.getConfig().getDouble(path + ".y");
        double z = plugin.getConfig().getDouble(path + ".z");
        float yaw = (float) plugin.getConfig().getDouble(path + ".yaw");
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch");
        return new Location(Bukkit.getWorld("world"), x, y, z, yaw, pitch);
    }
}
package me.jjkuhc.jjkcompass;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetCompassCommand implements CommandExecutor {
    private JavaPlugin plugin;

    public SetCompassCommand(JavaPlugin plugin) {
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

        if (cmd.equals("setspawn")) {
            saveLocationToConfig("spawn", player.getLocation());
            player.sendMessage("§aSpawn défini !");
        }

        if (cmd.equals("setjump")) {
            saveLocationToConfig("jump", player.getLocation());
            player.sendMessage("§aJump défini !");
        }

        return true;
    }

    private void saveLocationToConfig(String path, Location loc) {
        plugin.getConfig().set(path + ".x", loc.getX());
        plugin.getConfig().set(path + ".y", loc.getY());
        plugin.getConfig().set(path + ".z", loc.getZ());
        plugin.getConfig().set(path + ".yaw", loc.getYaw());
        plugin.getConfig().set(path + ".pitch", loc.getPitch());
        plugin.saveConfig();
    }
}
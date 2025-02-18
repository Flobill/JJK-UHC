package me.jjkuhc.jjkconfig;

import me.jjkuhc.host.HostManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande !");
            return true;
        }

        Player player = (Player) sender;

        // Vérifie si le joueur est host
        if (!HostManager.isHost(player)) {
            player.sendMessage("§cSeul l'host peut accéder au menu de configuration !");
            return true;
        }

        // Ouvre le menu seulement si l'argument est bien "config"
        if (args.length == 1 && args[0].equalsIgnoreCase("config")) {
            player.sendMessage("§aOuverture du menu de configuration...");
            new ConfigMenu().open(player);
        } else {
            player.sendMessage("§cUtilisation correcte : /jjk config");
        }

        return true;
    }
}

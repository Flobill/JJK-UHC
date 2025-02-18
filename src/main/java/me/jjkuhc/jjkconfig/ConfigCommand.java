package me.jjkuhc.jjkconfig;

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

        // Vérifie que l'argument est bien "config"
        if (args.length == 1 && args[0].equalsIgnoreCase("config")) {
            new ConfigMenu().open(player); // Ouvre le menu
        } else {
            player.sendMessage("§cUtilisation correcte : /jjk config");
        }

        return true;
    }
}
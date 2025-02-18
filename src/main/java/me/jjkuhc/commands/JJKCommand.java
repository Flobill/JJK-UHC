package me.jjkuhc.commands;

import me.jjkuhc.jjkconfig.ConfigCommand;
import me.jjkuhc.host.HostCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JJKCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande !");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUtilisation correcte : /jjk <config|sethost>");
            return true;
        }

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("config")) {
            return new ConfigCommand().onCommand(sender, command, label, args);
        } else if (args[0].equalsIgnoreCase("sethost")) {
            return new HostCommand().onCommand(sender, command, label, args);
        } else {
            player.sendMessage("§cCommande inconnue. Utilisation : /jjk <config|sethost>");
            return true;
        }
    }
}

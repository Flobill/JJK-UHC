package me.jjkuhc.commands;

import me.jjkuhc.jjkconfig.ConfigCommand;
import me.jjkuhc.host.HostCommand;
import me.jjkuhc.jjkroles.neutres.Sukuna; // Import pour appeler la commande de Sukuna
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
            sender.sendMessage("§cUtilisation correcte : /jjk <config|sethost|recuperer>");
            return true;
        }

        Player player = (Player) sender;

        // ✅ Gestion des différentes sous-commandes
        switch (args[0].toLowerCase()) {
            case "config":
                return new ConfigCommand().onCommand(sender, command, label, args);

            case "sethost":
                return new HostCommand().onCommand(sender, command, label, args);

            case "recuperer":
                if (args.length < 2) {
                    player.sendMessage("§c❌ Utilisation correcte : /jjk recuperer <joueur>");
                    return true;
                }
                String targetName = args[1];
                Sukuna.initiateFingerSteal(player, targetName); // Appel de la commande de Sukuna
                return true;

            default:
                player.sendMessage("§cCommande inconnue. Utilisation : /jjk <config|sethost|recuperer>");
                return true;
        }
    }
}
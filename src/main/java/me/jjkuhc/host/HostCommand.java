package me.jjkuhc.host;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HostCommand implements CommandExecutor {

    private static Player host = null;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande !");
            return true;
        }

        Player player = (Player) sender;

        // Vérifie si la commande est bien /jjk sethost <joueur>
        if (args.length != 2) {
            player.sendMessage("§cUtilisation correcte : /jjk sethost <joueur>");
            return true;
        }

        // Récupère le joueur cible
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            player.sendMessage("§cLe joueur '" + args[1] + "' n'est pas connecté !");
            return true;
        }

        // Vérifie si le joueur est déjà host pour l'enlever
        if (host != null && host.equals(target)) {
            host = null;
            Bukkit.broadcastMessage("§c" + target.getName() + " n'est plus l'host de la partie !");
            return true;
        }

        // Définir l'host
        host = target;
        Bukkit.broadcastMessage("§6" + target.getName() + " est maintenant l'host de la partie !");

        return true;
    }

    public static Player getHost() {
        return host;
    }
}
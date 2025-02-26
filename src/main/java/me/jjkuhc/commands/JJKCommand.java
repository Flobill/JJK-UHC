package me.jjkuhc.commands;

import me.jjkuhc.jjkconfig.ConfigCommand;
import me.jjkuhc.host.HostCommand;
import me.jjkuhc.jjkconfig.PacteMenu;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameState;
import me.jjkuhc.jjkroles.RoleType;
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
            case "pacte":
                if (GameManager.getCurrentState() == GameState.EN_COURS) {
                    RoleType role = GameManager.getPlayerRole(player);
                    if (role == RoleType.ITADORI) {
                        PacteMenu.openPacteMenu(player);
                        return true;
                    } else {
                        player.sendMessage("§cSeul Itadori Yuji peut faire un pacte !");
                        return true;
                    }
                } else {
                    player.sendMessage("§cVous ne pouvez pas faire de pacte avant le début de la partie !");
                    return true;
                }

            default:
                player.sendMessage("§cCommande inconnue. Utilisation : /jjk <config|sethost|recuperer>");
                return true;
        }
    }
}
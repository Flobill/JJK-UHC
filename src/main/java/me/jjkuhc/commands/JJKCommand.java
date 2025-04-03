package me.jjkuhc.commands;

import me.jjkuhc.jjkconfig.ConfigCommand;
import me.jjkuhc.host.HostCommand;
import me.jjkuhc.jjkconfig.PacteMenu;
import me.jjkuhc.jjkgame.GameManager;
import me.jjkuhc.jjkgame.GameState;
import me.jjkuhc.jjkroles.CampManager;
import me.jjkuhc.jjkroles.CampType;
import me.jjkuhc.jjkroles.RoleType;
import me.jjkuhc.jjkroles.exorcistes.Nobara;
import me.jjkuhc.jjkroles.fleaux.Hanami;
import me.jjkuhc.jjkroles.neutres.Sukuna; // Import pour appeler la commande de Sukuna
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

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

            case "messagemort":
                me.jjkuhc.jjkgame.DeathManager.toggleDeathMessage();
                boolean state = me.jjkuhc.jjkgame.DeathManager.isDeathMessageEnabled();
                player.sendMessage("§eSystème de mort : " + (state ? "§aActivé" : "§cDésactivé"));
                return true;

            case "compo":
                Map<CampType, List<String>> rolesParCamp = new HashMap<>();

                for (CampType camp : CampType.values()) {
                    Set<RoleType> rolesActifs = CampManager.getInstance().getActiveRoles(camp);
                    List<String> noms = new ArrayList<>();
                    for (RoleType role : rolesActifs) {
                        noms.add(role.getDisplayName());
                    }
                    rolesParCamp.put(camp, noms);
                }

                player.sendMessage("§7==========================================");
                for (CampType camp : List.of(CampType.EXORCISTES, CampType.FLEAUX, CampType.NEUTRES)) {
                    List<String> noms = rolesParCamp.getOrDefault(camp, new ArrayList<>());
                    String ligne = switch (camp) {
                        case EXORCISTES -> "§aExorcistes";
                        case FLEAUX -> "§cFléaux";
                        case NEUTRES -> "§6Neutres";
                        default -> "§f" + camp.name();
                    };

                    String couleur = switch (camp) {
                        case EXORCISTES -> "§a";
                        case FLEAUX -> "§c";
                        case NEUTRES -> "§6";
                        default -> "§f";
                    };

                    player.sendMessage(couleur + camp.getDisplayName() + " (" + couleur + noms.size() + couleur + ") : §f" + String.join(", ", noms));
                }
                player.sendMessage("§7==========================================");
                return true;

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
                        player.sendMessage("§f[§9JJK UHC§f] §aOuverture du menu de pacte...");
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
            case "nobara":
                if (args.length < 2) {
                    player.sendMessage("§c❌ Utilisation correcte : /jjk nobara <joueur>");
                    return true;
                }

                if (GameManager.getPlayerRole(player) != RoleType.NOBARA) {
                    player.sendMessage("§c❌ Seule Nobara Kugisaki peut utiliser cette commande !");
                    return true;
                }

                Player cible = player.getServer().getPlayer(args[1]);
                if (cible == null) {
                    player.sendMessage("§c❌ Ce joueur n'est pas en ligne !");
                    return true;
                }

                Nobara nobara = new Nobara(player);
                nobara.activerPartageDouleurs(player, cible);
                return true;

            case "clou":
                if (args.length < 2) {
                    player.sendMessage("§c❌ Utilisation correcte : /jjk clou <joueur>");
                    return true;
                }

                // Vérifier si le joueur a le rôle de Nobara
                if (GameManager.getPlayerRole(player) != RoleType.NOBARA) {
                    player.sendMessage("§c❌ Seule Nobara Kugisaki peut utiliser cette commande !");
                    return true;
                }

                Player target = player.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§c❌ Ce joueur n'est pas en ligne !");
                    return true;
                }
                Nobara.poserClou(player, target);
                return true;

            case "bourgeon":
                if (args.length < 2) {
                    player.sendMessage("§c❌ Utilisation correcte : /jjk bourgeon <joueur>");
                    return true;
                }

                // Vérifier si le joueur a le rôle Hanami
                if (GameManager.getPlayerRole(player) != RoleType.HANAMI) {
                    player.sendMessage("§c❌ Seul Hanami peut utiliser cette commande !");
                    return true;
                }

                Player cibleBourgeon = player.getServer().getPlayer(args[1]);
                if (cibleBourgeon == null) {
                    player.sendMessage("§c❌ Ce joueur n'est pas en ligne !");
                    return true;
                }
                // Appel de la fonction utiliserBourgeon dans Hanami
                Hanami hanami = Hanami.getHanamiInstance(player);
                hanami.utiliserBourgeon(cibleBourgeon);
                return true;

            case "mort":
                if (GameManager.getPlayerRole(player) != RoleType.GETO) {
                    player.sendMessage("§c❌ Seul Suguru Geto peut utiliser cette commande !");
                    return true;
                }

                me.jjkuhc.jjkroles.fleaux.Geto.getGetoInstance(player).simulerMort();
                return true;

            default:
                player.sendMessage("§cCommande inconnue. Utilisation : /jjk <config / sethost / recuperer / pacte / nobara / clou / bourgeon / mort>");
                return true;
        }
    }
}
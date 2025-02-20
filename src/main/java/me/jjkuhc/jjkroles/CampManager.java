package me.jjkuhc.jjkroles;

import java.util.*;

public class CampManager {
    private static CampManager instance;

    // Liste des rôles activés pour chaque camp
    private final Map<CampType, Set<RoleType>> activeRoles;

    // Camp actuellement affiché dans le menu
    private CampType currentCamp;

    // Singleton (1 seule instance du CampManager)
    public static CampManager getInstance() {
        if (instance == null) {
            instance = new CampManager();
        }
        return instance;
    }

    private CampManager() {
        activeRoles = new HashMap<>();
        for (CampType camp : CampType.values()) {
            activeRoles.put(camp, new HashSet<>());
        }
        currentCamp = CampType.EXORCISTES; // Camp par défaut
    }

    // Récupérer les rôles activés d'un camp
    public Set<RoleType> getActiveRoles(CampType camp) {
        return activeRoles.getOrDefault(camp, new HashSet<>());
    }

    // Activer ou désactiver un rôle
    public void toggleRole(CampType camp, RoleType role) {
        Set<RoleType> roles = activeRoles.get(camp);
        if (roles.contains(role)) {
            roles.remove(role); // Désactiver le rôle
        } else {
            roles.add(role); // Activer le rôle
        }
    }

    // Définir le camp actuellement affiché
    public void setCurrentCamp(CampType camp) {
        currentCamp = camp;
    }

    // Obtenir le camp actuellement affiché
    public CampType getCurrentCamp() {
        return currentCamp;
    }
}
package me.jjkuhc.jjkroles;

public enum RoleType {
    GOJO(CampType.EXORCISTES, "Gojo"),
    SUKUNA(CampType.FLEAUX, "Sukuna"),
    MEGUMI(CampType.FLEAUX, "Megumi"),
    YUTA(CampType.EXORCISTES, "Yuta"),
    TOJI(CampType.NEUTRES, "Toji"),
    EXORCISTE_BASIQUE(CampType.EXORCISTES, "Exorciste Basique"); // Par défaut exorciste

    private final CampType camp;
    private final String displayName;

    // ✅ Constructeur combiné
    RoleType(CampType camp, String displayName) {
        this.camp = camp;
        this.displayName = displayName;
    }

    // ✅ Méthodes d'accès
    public CampType getCamp() {
        return camp;
    }

    public String getDisplayName() {
        return displayName;
    }
}
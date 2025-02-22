package me.jjkuhc.jjkroles;

public enum RoleType {
    GOJO(CampType.EXORCISTES, "Gojo"),
    SUKUNA(CampType.FLEAUX, "Sukuna"),
    MEGUMI(CampType.FLEAUX, "Megumi"),
    YUTA(CampType.YUTA_RIKA, "Yuta Okkotsu"),
    RIKA(CampType.YUTA_RIKA, "Rika Orimoto"),
    TOJI(CampType.NEUTRES, "Toji"),
    JOGO(CampType.NEUTRES, "Jogo"),
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
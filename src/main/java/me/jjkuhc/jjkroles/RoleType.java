package me.jjkuhc.jjkroles;

public enum RoleType {
    GOJO(CampType.EXORCISTES, "Gojo"),
    ITADORI(CampType.EXORCISTES, "Itadori"),
    MEGUMI(CampType.EXORCISTES, "Megumi"),
    NOBARA(CampType.EXORCISTES, "Nobara"),
    JOGO(CampType.FLEAUX, "Jogo"),
    SUKUNA(CampType.NEUTRES, "Sukuna"),
    TOJI(CampType.NEUTRES, "Toji"),
    YUTA(CampType.YUTA_RIKA, "Yuta Okkotsu"),
    RIKA(CampType.YUTA_RIKA, "Rika Orimoto"),

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
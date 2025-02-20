package me.jjkuhc.jjkroles;

public enum RoleType {
    GOJO("Gojo"),
    SUKUNA("Sukuna"),
    MEGUMI("Megumi"),
    YUTA("Yuta"),
    TOJI("Toji"),
    EXORCISTE_BASIQUE("Exorciste Basique");

    private final String displayName;

    RoleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
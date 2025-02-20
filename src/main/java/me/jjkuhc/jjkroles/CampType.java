package me.jjkuhc.jjkroles;

public enum CampType {
    EXORCISTES("Exorcistes"),
    FLEAUX("Fléaux"),
    NEUTRES("Neutres");

    private final String displayName;

    CampType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
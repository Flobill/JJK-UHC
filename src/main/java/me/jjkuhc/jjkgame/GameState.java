package me.jjkuhc.jjkgame;

public enum GameState {
    EN_ATTENTE("En attente"),
    EN_LANCEMENT("En lancement"),
    EN_COURS("En cours"),
    FINIE("Finie");

    private final String displayName;

    GameState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static GameState currentState = EN_ATTENTE;

    public static GameState getCurrentState() {
        return currentState;
    }

    public static void setGameState(GameState newState) {
        currentState = newState;
    }
}
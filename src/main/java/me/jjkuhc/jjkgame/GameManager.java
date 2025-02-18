package me.jjkuhc.jjkgame;

public class GameManager {
    private static GameState currentState = GameState.EN_ATTENTE; // Par défaut en attente

    public static GameState getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(GameState newState) {
        currentState = newState;
    }

    public static boolean isState(GameState state) {
        return currentState == state;
    }
}
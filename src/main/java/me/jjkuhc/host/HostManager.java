package me.jjkuhc.host;

import org.bukkit.entity.Player;

public class HostManager {
    private static Player host = null;

    public static void setHost(Player player) {
        host = (host == player) ? null : player;
    }

    public static boolean isHost(Player player) {
        return host != null && host.equals(player);
    }

    public static Player getHost() {
        return host;
    }
}
package com.borgdude.paintball.database;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlayerStats {
    public UUID id;
    public int kills;
    public int wins;
    public String name;

    public PlayerStats(UUID id, int kills, int wins) {
        this.id = id;
        this.kills = kills;
        this.wins = wins;
        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
        this.name = op.getName();
    }

    public String toString() {
        return name + ", " + kills + ", " + wins;
    }
}

package com.borgdude.paintball.database;

import java.util.UUID;

public class PlayerStats {
	public UUID id;
	public int kills;
	public int wins;
	
	public PlayerStats(UUID id, int kills, int wins) {
		this.id = id;
		this.kills = kills;
		this.wins = wins;
	}
}

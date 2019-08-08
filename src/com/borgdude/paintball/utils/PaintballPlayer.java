package com.borgdude.paintball.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PaintballPlayer {
    private Inventory inv;
    private Player p;
    private GameMode gameMode;
    private float xp;
    private int level;

    public PaintballPlayer(Player p) {
        this.p = p;
        registerInfo();
    }

    public void registerInventory() {
        inv = Bukkit.createInventory(null, 9 * 6);
        for (int i = 0; i < inv.getSize(); i++) {
            try {
                if (p.getInventory().getItem(i) != null) {
                    inv.setItem(i, p.getInventory().getItem(i));
                }
            } catch (Exception e) {
                break;
            }
        }
    }

    public void setPlayerInventory() {
        if (inv == null) {
            return;
        }
        for (int i = 0; i < 9 * 6; i++) {
            if (inv.getItem(i) != null) {
                p.getInventory().setItem(i, inv.getItem(i));
            }
        }
    }

    public void registerGameMode() {
        this.gameMode = p.getGameMode();
    }

    public void setPlayerGameMode() {
        if (gameMode != null) {
            p.setGameMode(gameMode);
        }
    }

    public void registerInfo() {
        registerInventory();
        registerGameMode();
        registerLevels();
    }

    private void registerLevels() {
        this.xp = p.getExp();
        this.level = p.getLevel();
    }

    public void setInfo() {
        setPlayerGameMode();
        setPlayerInventory();
        setLevels();
    }

    private void setLevels() {
        p.setExp(this.xp);
        p.setLevel(this.level);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((p == null) ? 0 : p.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PaintballPlayer other = (PaintballPlayer) obj;
        if (p == null) {
            if (other.p != null)
                return false;
        } else if (!p.equals(other.p))
            return false;
        return true;
    }

}

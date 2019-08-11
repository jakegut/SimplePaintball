package com.borgdude.paintball.managers;

import com.borgdude.paintball.objects.Gun;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PaintballManager {

    private HashMap<Player, Integer> cooldown;
    private Map<Integer, BukkitTask> projectiles = new HashMap<Integer, BukkitTask>();
    private Set<Gun> guns;

    public PaintballManager() {
        cooldown = new HashMap<>();
        projectiles = new HashMap<>();
        guns = new HashSet<>();
    }

    public Set<Gun> getGuns() {
        return guns;
    }

    public HashMap<Player, Integer> getCooldown() {
        return cooldown;
    }

    public void setCooldown(HashMap<Player, Integer> cooldown) {
        this.cooldown = cooldown;
    }

    public Map<Integer, BukkitTask> getProjectiles() {
        return projectiles;
    }

    public void setProjectiles(Map<Integer, BukkitTask> projectiles) {
        this.projectiles = projectiles;
    }

    public void registerGun(Gun gun) {
        getGuns().add(gun);
    }

    public Gun getGunByName(String n) {
        for (Gun gun : getGuns()) {
            if (gun.getName().equalsIgnoreCase(n))
                return gun;
        }

        return null;
    }
}

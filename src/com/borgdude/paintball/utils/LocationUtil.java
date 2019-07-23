package com.borgdude.paintball.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class LocationUtil {

    public static void saveLocation(String p, Location location, FileConfiguration config)
    {
        config.set(p + ".X", Integer.valueOf(location.getBlockX()));
        config.set(p + ".Y", Integer.valueOf(location.getBlockY()));
        config.set(p + ".Z", Integer.valueOf(location.getBlockZ()));
        config.set(p + ".Yaw", Float.valueOf(location.getYaw()));
        config.set(p + ".Pitch", Float.valueOf(location.getPitch()));
        config.set(p + ".World", location.getWorld().getName());
    }

    public static Location getLocation(ConfigurationSection section)
    {
        try
        {
            return new Location(Bukkit.getWorld(section.getString("World")), section.getInt("X"), section.getInt("Y"), section.getInt("Z"));
        }
        catch (Exception e) {}
        return null;
    }

    public static Location getLocationWithDirection(ConfigurationSection section)
    {
        try
        {
            return new Location(Bukkit.getWorld(section.getString("World")), section.getInt("X"), section.getInt("Y"), section.getInt("Z"), section.getInt("Yaw"), section.getInt("Pitch"));
        }
        catch (Exception e) {}
        return null;
    }

}

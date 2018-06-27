package com.borgdude.paintball.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class LocationUtil {

    public static void saveLocation(String Path, Location location, Plugin plugin)
    {
        plugin.getConfig().set(Path + ".X", Integer.valueOf(location.getBlockX()));
        plugin.getConfig().set(Path + ".Y", Integer.valueOf(location.getBlockY()));
        plugin.getConfig().set(Path + ".Z", Integer.valueOf(location.getBlockZ()));
        plugin.getConfig().set(Path + ".Yaw", Float.valueOf(location.getYaw()));
        plugin.getConfig().set(Path + ".Pitch", Float.valueOf(location.getPitch()));
        plugin.getConfig().set(Path + ".World", location.getWorld().getName());
        plugin.saveConfig();
    }

    public static Location getLocation(String Path, Plugin plugin)
    {
        try
        {
            return new Location(Bukkit.getWorld(plugin.getConfig().getString(Path + ".World")), plugin.getConfig().getInt(Path + ".X"), plugin.getConfig().getInt(Path + ".Y"), plugin.getConfig().getInt(Path + ".Z"));
        }
        catch (Exception e) {}
        return null;
    }

    public static Location getLocationWithDirection(String Path, Plugin plugin)
    {
        try
        {
            return new Location(Bukkit.getWorld(plugin.getConfig().getString(Path + ".World")), plugin.getConfig().getInt(Path + ".X"), plugin.getConfig().getInt(Path + ".Y"), plugin.getConfig().getInt(Path + ".Z"), plugin.getConfig().getInt(Path + ".Yaw"), plugin.getConfig().getInt(Path + ".Pitch"));
        }
        catch (Exception e) {}
        return null;
    }

}

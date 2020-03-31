package com.borgdude.paintball.utils;

import com.borgdude.paintball.Main;
import org.bukkit.ChatColor;

public class LoggerUtil {

    private Main plugin;

    public LoggerUtil(Main plugin){
        this.plugin = plugin;
    }

    public void log(String message){
        if(plugin.getConfig().getBoolean("Debug"))
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + message);

    }
}

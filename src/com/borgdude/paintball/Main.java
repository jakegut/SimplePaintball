package com.borgdude.paintball;

import com.borgdude.paintball.commands.GunCommand;
import com.borgdude.paintball.commands.PaintballCommand;
import com.borgdude.paintball.events.EventClass;
import com.borgdude.paintball.managers.ArenaManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Main extends JavaPlugin {

    public static Main plugin;
    public static ArenaManager arenaManager;

    @Override
    public void onEnable(){
        plugin = this;
        getConfig().options().copyDefaults(true);
        saveConfig();
        arenaManager = new ArenaManager(new ArrayList<>(), this);
        arenaManager.getArenas();
        getCommand("gun").setExecutor(new GunCommand());
        getCommand("pb").setExecutor(new PaintballCommand());
        getServer().getPluginManager().registerEvents(new EventClass(), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "PaintBall Enabled");
    }

    @Override
    public void onDisable(){
        arenaManager.saveArenas();
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "PaintBall Disabled");
    }
}

package com.borgdude.paintball;

import com.borgdude.paintball.commands.GunCommand;
import com.borgdude.paintball.commands.PaintballCommand;
import com.borgdude.paintball.events.EventClass;
import com.borgdude.paintball.managers.ArenaManager;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.guns.*;

import net.md_5.bungee.api.ChatColor;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.ArrayList;

public class Main extends JavaPlugin {

    public static Main plugin;
    public static ArenaManager arenaManager;
    public static PaintballManager paintballManager;

    @Override
    public void onEnable(){
        plugin = this;
        Metrics metrics = new Metrics(this);
        saveDefaultConfig();
        paintballManager = new PaintballManager();
        paintballManager.registerGun(new Admin());
        paintballManager.registerGun(new Sniper());
        paintballManager.registerGun(new Rocket());
        paintballManager.registerGun(new Minigun());
        paintballManager.registerGun(new Shotgun());
        arenaManager = new ArenaManager(new ArrayList<>(), this);
        arenaManager.getArenas();
        getCommand("gun").setExecutor(new GunCommand());
        getCommand("pb").setExecutor(new PaintballCommand());
        getServer().getPluginManager().registerEvents(new EventClass(), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "PaintBall Enabled");
    }

    @Override
    public void onDisable(){
    	for(Arena arena : arenaManager.getArena()) {
			arena.kickPlayers();
    	}
        arenaManager.saveArenas();
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "PaintBall Disabled");
    }
}

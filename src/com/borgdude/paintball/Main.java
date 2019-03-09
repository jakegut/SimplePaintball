package com.borgdude.paintball;

import com.borgdude.paintball.commands.*;
import com.borgdude.paintball.events.EventClass;
import com.borgdude.paintball.managers.*;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.guns.*;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.ArrayList;

public class Main extends JavaPlugin {

    public static Main plugin;
    public static ArenaManager arenaManager;
    public static PaintballManager paintballManager;
    public static InventoryManager inventoryManager;
    
    public static Economy econ = null;

    @Override
    public void onEnable(){
        plugin = this;
        Metrics metrics = new Metrics(this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        if(!setupEconomy()) {
        	getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Vault Economy Disapled");
        }
        inventoryManager = new InventoryManager(this);
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
        getCommand("pb").setTabCompleter(new PaintballCompleter());
        getServer().getPluginManager().registerEvents(new EventClass(), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "PaintBall Enabled");
    }
    
    // From https://github.com/MilkBowl/VaultAPI/
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable(){
    	for(Arena arena : arenaManager.getArena()) {
			arena.kickPlayers();
    	}
    	reloadConfig();
        arenaManager.saveArenas();
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "PaintBall Disabled");
    }
}

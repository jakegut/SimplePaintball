package com.borgdude.paintball;

import com.borgdude.paintball.commands.*;
import com.borgdude.paintball.database.Database;
import com.borgdude.paintball.database.SQLite;
import com.borgdude.paintball.events.EventClass;
import com.borgdude.paintball.managers.*;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.guns.*;

import com.borgdude.paintball.utils.LoggerUtil;
import net.milkbowl.vault.economy.Economy;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends JavaPlugin {

    public static Main plugin;
    private ArenaManager arenaManager;
    private PaintballManager paintballManager;
    private InventoryManager inventoryManager;

    private LanguageManager languageManager;

    private LoggerUtil logger;

    private File arenaConfigFile;
    private FileConfiguration arenaConfig;
    public static Database db;

    public static Metrics metrics = null;
    public static Economy econ = null;

    @Override
    public void onEnable() {
        plugin = this;
        metrics = new Metrics(this);
        logger = new LoggerUtil(this);
        try {
            createCustomConfigs();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            getServer().getConsoleSender()
                    .sendMessage(ChatColor.RED + "Simple Paintball ran to an error ... disabling");
            return;
        }
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        matchConfig();
        languageManager = new LanguageManager(this);
        if (!setupEconomy()) {
            getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Vault Economy Disabled");
        }
        if (getConfig().getBoolean("Stats.Track")) {
            setDb(new SQLite(this));
            db.load();
        } else {
            getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Simple Paintball Stats Disabled");
        }
        inventoryManager = new InventoryManager(this);
        paintballManager = new PaintballManager();
        paintballManager.registerGun(new Admin(this));
        paintballManager.registerGun(new Sniper(this));
        paintballManager.registerGun(new Rocket(this));
        paintballManager.registerGun(new Minigun(this));
        paintballManager.registerGun(new Shotgun());
        getServer().getServicesManager().register(PaintballManager.class, this.paintballManager, this, ServicePriority.Normal);
        arenaManager = new ArenaManager(new HashMap<>(), this);
        arenaManager.getArenas();
        getCommand("gun").setExecutor(new GunCommand(this));
        getCommand("pb").setExecutor(new PaintballCommand(this));
        getCommand("pb").setTabCompleter(new PaintballCompleter(this));
        getServer().getPluginManager().registerEvents(new EventClass(this), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "PaintBall Enabled");

    }

    public LoggerUtil getPluginLogger() { return this.logger; }

    public PaintballManager getPaintballManager() {
        return this.paintballManager;
    }

    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    private void createCustomConfigs() throws FileNotFoundException, IOException, InvalidConfigurationException {
        arenaConfigFile = new File(getDataFolder(), "arenas.yml");
        if (!arenaConfigFile.exists()) {
            arenaConfigFile.getParentFile().mkdirs();
            saveResource("arenas.yml", false);
            arenaConfig = YamlConfiguration.loadConfiguration(arenaConfigFile);
            arenaConfig.addDefault("arenas", " ");
            arenaConfig.options().copyDefaults(true);
        } else {
            arenaConfig = YamlConfiguration.loadConfiguration(arenaConfigFile);
        }
    }

    public FileConfiguration getArenaConfig() {
        return this.arenaConfig;
    }

    public void saveArenaConfig() throws IOException {
        getArenaConfig().save(this.arenaConfigFile);
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

    // From
    // https://bukkit.org/threads/make-your-custom-config-update-and-copy-delete-the-defaults.373956/
    private void matchConfig() {
        try {
            File file = new File(plugin.getDataFolder().getAbsolutePath(), "config.yml");
            if (file != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
                for (String key : defConfig.getConfigurationSection("").getKeys(false))
                    if (!getConfig().contains(key))
                        getConfig().set(key, defConfig.getConfigurationSection(key));

                saveConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        for (Arena arena : arenaManager.getArena().values()) {
            arena.kickPlayers();
        }
        reloadConfig();
        arenaManager.saveArenas();
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "PaintBall Disabled");
    }

    public Database getDb() {
        return db;
    }

    public void setDb(Database db) {
        this.db = db;
    }

}

package com.borgdude.paintball.managers;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.borgdude.paintball.Main;

public class LanguageManager {

    final String DEFAULT_LOCALE = "enUS";

    private FileConfiguration languages;
    private Main plugin;

    public LanguageManager(Main main) {
        this.plugin = main;
        plugin.saveResource("languages/enUS.yml", true);
        loadLanguage();
    }

    private void loadLanguage() {
        String loc = plugin.getConfig().getString("locale");
        File langFile = loadLanguage(loc);
        if (langFile == null) {
            return;
        } else if (langFile.exists()) {
            System.out.println("Using locale: " + langFile.getName());
        }

        languages = YamlConfiguration.loadConfiguration(langFile);
    }

    private File loadLanguage(String loc) {
        File langFile = new File(plugin.getDataFolder(), "languages/" + loc + ".yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            try {
                plugin.saveResource("languages/" + loc + ".yml", false);
            } catch (IllegalArgumentException e) {
                plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Language locale " + ChatColor.YELLOW
                        + loc + ChatColor.RED + " not found... Reverting to: " + ChatColor.YELLOW + DEFAULT_LOCALE);
                if (loc == "enUS")
                    return null;
                return loadLanguage("enUS");
            }
        }
        return langFile;

    }

    public String getMessage(String location) {
        String original = languages.getString(location);
        if (original == null)
            return ChatColor.RED + "Message not found!! Check your locale file.";

        return ChatColor.translateAlternateColorCodes('&', original);
    }
}

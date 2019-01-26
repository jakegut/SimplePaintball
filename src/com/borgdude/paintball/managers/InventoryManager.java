package com.borgdude.paintball.managers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.borgdude.paintball.Main;

public class InventoryManager {
	private Main plugin;
	
	public InventoryManager(Main plugin) {
		this.plugin = plugin;
	}
	
	public void saveInventory(Player p) throws IOException {
        File f = new File(plugin.getDataFolder().getAbsolutePath(), p.getName() + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set("inventory.armor", p.getInventory().getArmorContents());
        c.set("inventory.content", p.getInventory().getContents());
        c.set("inventory.level", p.getLevel());
        c.set("inventory.exp", p.getExp());
        c.save(f);
    }

    @SuppressWarnings("unchecked")
    public void restoreInventory(Player p) throws IOException {
        File f = new File(plugin.getDataFolder().getAbsolutePath(), p.getName() + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        ItemStack[] content = ((List<ItemStack>) c.get("inventory.armor")).toArray(new ItemStack[0]);
        p.getInventory().setArmorContents(content);
        content = ((List<ItemStack>) c.get("inventory.content")).toArray(new ItemStack[0]);
        p.getInventory().setContents(content);
        p.setLevel(c.getInt("inventory.level"));
        p.setExp((float) c.getDouble("inventory.exp"));
    }
}

package com.borgdude.paintball.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public enum GunKit {
	
    ADMIN("Admin", 2), REGULAR("Sniper", 8), SHOTGUN("Shotgun", 15), MINIGUN("Minigun", 2), LAUNCHER("Rocket Launcher", 35);

    String formattedName;
    int fireRate;

    GunKit(String formattedName, int fireRate) {
        this.formattedName = formattedName;
        this.fireRate = fireRate;
    }
    
    public int getFireRate() {
    	return fireRate;
    }

    public String getFormattedName() {
        return formattedName;
    }
    
    static public ItemStack getStack(GunKit kit) {
    	ItemStack is;
    	ItemMeta im;
    	
    	switch(kit) {
		case ADMIN:
			is = new ItemStack(Material.DIAMOND_HOE);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.AQUA + "Admin Gun");
			is.setItemMeta(im);
			break;
		case REGULAR:
			is = new ItemStack(Material.GOLD_HOE);
            im = is.getItemMeta();
            im.setDisplayName(ChatColor.GOLD + "PaintBall Sniper");
            is.setItemMeta(im);
            break;
		case SHOTGUN:
			is = new ItemStack(Material.IRON_HOE);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.GRAY + "PaintBall Shotgun");
			is.setItemMeta(im);
			break;
		case MINIGUN:
			is = new ItemStack(Material.STONE_HOE);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.LIGHT_PURPLE + "PaintBall MiniGun");
			is.setItemMeta(im);
			break;
		case LAUNCHER:
			is = new ItemStack(Material.IRON_SPADE);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.DARK_GREEN + "PaintBall Rocket Launcher");
			is.setItemMeta(im);
			break;
		default:
			is = null;
			break;  	
    	}

    	return is;
    }
}
package com.borgdude.paintball.commands;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.objects.Gun;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GunCommand implements CommandExecutor {

    private final Main plugin;
    
    public GunCommand(Main p) {
    	this.plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;

            if(!player.hasPermission("paintball.admin")){
                player.sendMessage(ChatColor.RED + "You're not an admin!");
                return true;
            }

            if(command.getName().equalsIgnoreCase("gun")){
            	if(args.length >= 1) {
            		Gun gun = plugin.getPaintballManager().getGunByName(args[0]);
            		if(gun == null) {
            			player.sendMessage(ChatColor.RED + "Gun not found.");
            			return true;
            		}
            		
            		ItemStack is = gun.getInGameItem();
            		player.getInventory().addItem(is);
            		return true;
            	}
                ItemStack is = plugin.getPaintballManager().getGunByName("Admin").getInGameItem();
                player.getInventory().addItem(is);
                return true;
            }
        }
        return false;
    }
}

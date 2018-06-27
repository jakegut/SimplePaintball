package com.borgdude.paintball.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GunCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;

            if(command.getName().equalsIgnoreCase("gun")){
                ItemStack is = new ItemStack(Material.GOLD_HOE);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName(ChatColor.GOLD + "PaintBall Gun");
                is.setItemMeta(im);
                player.getInventory().addItem(is);
                return true;
            }
        }
        return false;
    }
}

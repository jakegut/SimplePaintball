package com.borgdude.paintball.objects.guns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.objects.Gun;
import com.borgdude.paintball.utils.MathUtil;


public class Minigun implements Gun{
	
	private PaintballManager paintballManager = Main.paintballManager;
	private Main plugin = Main.plugin;

	@Override
	public ItemStack getLobbyItem() {
		ItemStack is = new ItemStack(Material.PURPLE_WOOL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.LIGHT_PURPLE + "Minigun");
		is.setItemMeta(im);
		return is;
	}

	@Override
	public ItemStack getInGameItem() {
		ItemStack is = new ItemStack(Material.WOODEN_HOE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.LIGHT_PURPLE + "PaintBall Minigun");
		is.setItemMeta(im);
		return is;
	}

	@Override
	public int getCooldown() {
		return 2;
	}

	@Override
	public void fire(Player player) {
    	
    	float accuracy = 0.3F;
        
        Snowball snowball = null;
        Vector velocity = null;
        
        snowball = player.launchProjectile(Snowball.class); //set the snowball variable
        velocity = player.getLocation().getDirection().multiply(0.9); //set the velocity variable
        velocity.add(MathUtil.getRandomVector(accuracy)); //set it's modified velocity
//        velocity.add(new Vector(0.25, 0.12, 0.25));
        snowball.setVelocity(velocity);
        
//        player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
        
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Snowball snowball = player.launchProjectile(Snowball.class); //set the snowball variable
                Vector velocity = player.getLocation().getDirection().multiply(0.9); //set the velocity variable
                velocity.add(MathUtil.getRandomVector(accuracy)); //set it's modified velocity
//                velocity.add(new Vector(0.25, 0.12, 0.25));
                snowball.setVelocity(velocity);
                
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 2, 1.5f); 
			}
        	
        }, 2L);
		
	}

	@Override
	public void onHit(Player player, Snowball ball) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Minigun";
	}

}

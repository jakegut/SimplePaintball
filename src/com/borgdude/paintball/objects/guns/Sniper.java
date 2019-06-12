package com.borgdude.paintball.objects.guns;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.objects.Gun;

public class Sniper implements Gun{
	
	private Main plugin = Main.plugin;
	private PaintballManager paintballManager = Main.paintballManager;

	@Override
	public ItemStack getLobbyItem() {
        ItemStack reg = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta rm = reg.getItemMeta();
        rm.setDisplayName(ChatColor.YELLOW + "Sniper");
        reg.setItemMeta(rm);
		return reg;
	}

	@Override
	public ItemStack getInGameItem() {
		ItemStack is = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "PaintBall Sniper");
        is.setItemMeta(im);
		return is;
	}

	@Override
	public int getCooldown() {
		return 8;
	}

	@Override
	public void fire(Player player) {
        
        final Snowball snowball = player.launchProjectile(Snowball.class);
        final Vector velocity = player.getLocation().getDirection().multiply(2);//set the velocity variable
        snowball.setVelocity(velocity);
        
        paintballManager.getProjectiles().put(snowball.getEntityId(), new BukkitRunnable() {
        	 
            @Override
            public void run() {
                snowball.setVelocity(velocity);
            }
        }.runTaskTimer(plugin, 1, 1));
        
        player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2, 0.5f);
		
	}

	@Override
	public void onHit(Player player, Snowball ball) {
		player.getLocation().getWorld().playSound(ball.getLocation(), Sound.BLOCK_ANVIL_FALL, 1, 0.25f);
		
	}

	@Override
	public String getName() {
		return "Sniper";
	}

}

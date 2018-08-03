package com.borgdude.paintball.objects.guns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.objects.Gun;
import com.borgdude.paintball.utils.MathUtil;

import net.md_5.bungee.api.ChatColor;

public class Shotgun implements Gun {
	
	private PaintballManager paintballManager = Main.paintballManager;
	private Main plugin = Main.plugin;

	@Override
	public ItemStack getLobbyItem() {
		ItemStack shot = new ItemStack(Material.GRAY_WOOL);
        ItemMeta sm = shot.getItemMeta();
        sm.setDisplayName(ChatColor.GRAY + getName());
        shot.setItemMeta(sm);
		return shot;
	}

	@Override
	public ItemStack getInGameItem() {
		ItemStack is = new ItemStack(Material.IRON_HOE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GRAY + "PaintBall Shotgun");
		is.setItemMeta(im);
		return is;
	}

	@Override
	public int getCooldown() {
		return 15;
	}

	@Override
	public void fire(Player player) { 
    	
    	float accuracy = 0.15F;
        
        Snowball snowball = null;
        Vector velocity = null;
        
        for (int i = 0; i < 3; i++) { //Set i to 0 and as long as it is less than 5 add one to it then run the loop
            snowball = player.launchProjectile(Snowball.class); //set the snowball variable
            velocity = player.getLocation().getDirection().multiply(1.2); //set the velocity variable
            velocity.add(MathUtil.getRandomVector(accuracy)); //set it's modified velocity
//            velocity.add(new Vector(0.25, 0.12, 0.25));
            snowball.setVelocity(velocity); //set the snowball's new velocity
//                System.out.print(velocity.toString());
        }
        player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
		
	}

	@Override
	public void onHit(Player player, Snowball ball) {
		Vector s = ball.getVelocity().normalize().multiply(0.5);
		Block hitBlock = ball.getLocation().add(s).getBlock();
//		System.out.println(hitBlock.getLocation().toString());
//		System.out.println(hitBlock.getType().toString());
		Block landedBlock = ball.getLocation().getBlock();
//		System.out.println(landedBlock.getLocation().toString());
//		System.out.println(landedBlock.getType().toString());
		Vector n = landedBlock.getLocation().subtract(hitBlock.getLocation()).toVector().normalize();
		System.out.println(n.toString());
		Double d = 2 * n.dot(ball.getVelocity());
		System.out.println(d);
		Vector a = n.multiply(d);
		Vector r = ball.getVelocity().subtract(a);
		
		System.out.println(r.toString());
		
		Snowball nb = player.getWorld().spawn(ball.getLocation(), Snowball.class);
		nb.setVelocity(r);
		
		Bukkit.getConsoleSender().sendMessage(n.toString());
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Shotgun";
	}

}

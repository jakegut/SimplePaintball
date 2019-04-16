package com.borgdude.paintball.objects.guns;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.objects.Gun;
import com.borgdude.paintball.utils.MathUtil;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class Rocket implements Gun {

    private Main plugin = Main.plugin;
    private PaintballManager paintballManager = Main.paintballManager;

    @Override
    public ItemStack getLobbyItem() {
        ItemStack rocket = new ItemStack(Material.GREEN_WOOL);
        ItemMeta rocketM = rocket.getItemMeta();
        rocketM.setDisplayName(ChatColor.DARK_GREEN + "Rocket Launcher");
        rocket.setItemMeta(rocketM);
        return rocket;
    }

    @Override
    public ItemStack getInGameItem() {
        ItemStack is = new ItemStack(Material.IRON_SHOVEL);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.DARK_GREEN + "PaintBall Rocket Launcher");
        is.setItemMeta(im);
        return is;
    }

    @Override
    public int getCooldown() {
        return 35;
    }

    @Override
    public void fire(Player player) {

        Snowball snowball = null;
        Vector velocity = null;

        snowball = player.launchProjectile(Snowball.class); //set the snowball variable
        velocity = player.getLocation().getDirection();//set the velocity variable
//                velocity.add(new Vector(0.25, 0.12, 0.25));
        snowball.setVelocity(velocity);

        player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
    }

    @Override
    public void onHit(Player player, Snowball ball) {

        if (ball.hasMetadata("fired")) return;

        Location spawnLocation = ball.getLocation();

        int numberOfBalls = 8;
        int deg = 360 / numberOfBalls;

        for (int i = 0; i < numberOfBalls; i++) {
            Snowball snowball = player.getWorld().spawn(spawnLocation, Snowball.class);
            double vecX = Math.cos(Math.toRadians(i * deg));
            double vecY = 1;
            double vecZ = Math.sin(Math.toRadians(i * deg));
            snowball.setVelocity(new Vector(vecX, vecY, vecZ).multiply(0.35D).add(MathUtil.getRandomVector(0.2f)));
            snowball.setShooter(player);
            snowball.setMetadata("fired", new FixedMetadataValue(plugin, new Boolean(true)));
        }
    }

    @Override
    public String getName() {
        return "RocketLauncher";
    }
}

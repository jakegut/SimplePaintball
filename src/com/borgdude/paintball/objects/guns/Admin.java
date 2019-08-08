package com.borgdude.paintball.objects.guns;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.objects.Gun;
import com.borgdude.paintball.utils.MathUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.awt.*;

public class Admin implements Gun {

    private Main plugin;
    private PaintballManager paintballManager;

    public Admin(Main p) {
        this.plugin = p;
        this.paintballManager = p.getPaintballManager();
    }

    @Override
    public ItemStack getLobbyItem() {
        return null;
    }

    @Override
    public ItemStack getInGameItem() {
        ItemStack is;
        ItemMeta im;
        is = new ItemStack(Material.DIAMOND_HOE);
        im = is.getItemMeta();
        im.setDisplayName(ChatColor.AQUA + "Admin Gun");
        is.setItemMeta(im);
        return is;
    }

    @Override
    public int getCooldown() {
        return 2;
    }

    @Override
    public void fire(final Player player) {

        if (!player.hasPermission("paintball.admin"))
            return;

        final Snowball snowball = player.launchProjectile(Snowball.class);
        final Vector velocity = player.getLocation().getDirection().multiply(2);// set the velocity variable
        snowball.setVelocity(velocity);

        paintballManager.getProjectiles().put(snowball.getEntityId(), new BukkitRunnable() {

            @Override
            public void run() {
                snowball.setVelocity(velocity);
            }
        }.runTaskTimer(plugin, 1, 1));

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                final Snowball snowball = player.launchProjectile(Snowball.class);
                final Vector velocity = player.getLocation().getDirection().multiply(2);// set the velocity variable
                snowball.setVelocity(velocity);

                paintballManager.getProjectiles().put(snowball.getEntityId(), new BukkitRunnable() {

                    @Override
                    public void run() {
                        snowball.setVelocity(velocity);
                    }
                }.runTaskTimer(plugin, 1, 1));

                player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
            }

        }, 2L);
    }

    @Override
    public void onHit(Player player, Snowball ball) {

        if (ball.hasMetadata("fired")) {
            player.getLocation().getWorld().playSound(ball.getLocation(), Sound.BLOCK_ANVIL_FALL, 1, 0.25f);
            return;
        }

        player.getLocation().getWorld().playSound(ball.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.25f);

        Location spawnLocation = ball.getLocation();

        int numberOfBalls = 13;
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

    public boolean ifHigh() {
        return true;
    }

    @Override
    public String getName() {
        return "Admin";
    }
}

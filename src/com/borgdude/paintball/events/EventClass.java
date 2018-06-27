package com.borgdude.paintball.events;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.ArenaManager;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.ArenaState;
import com.borgdude.paintball.objects.Team;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EventClass implements Listener {

    private ArenaManager arenaManager = Main.arenaManager;
    private Main plugin = Main.plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(event.getItem() != null && event.getItem().getType().equals(Material.GOLD_HOE) &&
                    isNamedItem(event.getItem(), ChatColor.GOLD + "PaintBall Gun")){
                Player player = event.getPlayer();
                Snowball s = player.launchProjectile(Snowball.class);
                s.setVelocity(player.getLocation().getDirection().multiply(1.5D));
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event){
        Player shooter;
        Player hit;
        if(event.getHitEntity() instanceof Player && event.getEntity().getShooter() instanceof Player){
            shooter = (Player) event.getEntity().getShooter();
            hit = (Player) event.getHitEntity();
        } else {
            return;
        }


        if(shooter != null && hit != null && shooter instanceof Player && hit instanceof Player){
            Arena shooterA = arenaManager.getPlayerArena(shooter);
            Arena hitA = arenaManager.getPlayerArena(hit);

            if(shooterA == null || hitA == null){
                return;
            }

            if(shooterA.equals(hitA) && shooterA.getArenaState().equals(ArenaState.IN_GAME)){
                Team shooterTeam = shooterA.getPlayerTeam(shooter);
                Team hitTeam = shooterA.getPlayerTeam(hit);
                if(!(shooterTeam.equals(hitTeam))){
                    int prevKills = shooterA.getKills().get(shooter.getUniqueId());
                    shooterA.getKills().replace(shooter.getUniqueId(), prevKills + 1);
                    shooter.setLevel(prevKills + 1);
                    hit.getWorld().playSound(hit.getLocation(), Sound.BLOCK_STONE_HIT, 2, 0.5f);
                    hit.teleport(hitTeam.getRandomLocation());

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            ChatColor cs = shooterA.getPlayerTeam(shooter).equals(shooterA.getBlueTeam()) ? ChatColor.BLUE : ChatColor.RED;
                            ChatColor ch = cs.equals(ChatColor.BLUE) ? ChatColor.RED : ChatColor.BLUE;
                            for(UUID id : shooterA.getPlayers()){
                                Player p = Bukkit.getServer().getPlayer(id);
                                p.sendMessage(cs + shooter.getName() + ChatColor.GREEN + " killed " +
                                        ch + hit.getName());
                            }
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        Player p = event.getPlayer();
        if(this.arenaManager.getPlayerArena(p) != null){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChangeEvent(SignChangeEvent event){
        if(!event.getPlayer().hasPermission("paintball.admin")) return;

        if(event.getLine(0).equalsIgnoreCase("pb join")){
            if (event.getLine(1) == null) return;

            Arena a = this.arenaManager.getArenaByTitle(event.getLine(1));
            if(a == null){
                event.getPlayer().sendMessage(ChatColor.RED + "Arena with that name not found");
                event.setCancelled(true);
            } else {
                event.getPlayer().sendMessage(ChatColor.GREEN + "Added sign for arena " + ChatColor.AQUA + a.getTitle());
                a.getSigns().add((Sign) event.getBlock().getState());
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    a.updateSigns();
                }
            }.runTaskLater(this.plugin, 10);
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event){
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {

            Sign sign = (Sign) b.getState();
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[PaintBall]")) {

                Arena a = this.arenaManager.getArenaByTitle(ChatColor.stripColor(sign.getLine(1)));
                if (a == null) {
                    p.sendMessage(ChatColor.RED + "That arena wasn't found?!");
                } else {
                    Bukkit.dispatchCommand(p, "pb join " + a.getTitle());
                }
            }
        }

    }

    public static boolean isNamedItem(ItemStack item, String name) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(name))) {
            return true;
        }
        return false;
    }
}

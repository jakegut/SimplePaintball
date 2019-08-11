package com.borgdude.paintball.events;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.ArenaManager;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.objects.*;
import com.borgdude.paintball.utils.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.util.ArrayList;
import java.util.UUID;

public class EventClass implements Listener {

    private ArenaManager arenaManager;
    private PaintballManager paintballManager;
    private Main plugin;
    private int spawnTime;

    public EventClass(Main p) {
        super();
        spawnTime = p.getConfig().getInt("Gameplay.Spawn-Time");
        this.arenaManager = p.getArenaManager();
        this.paintballManager = p.getPaintballManager();
        this.plugin = p;
    }

    public void runTimer(final Player player, float fireRate) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                int timeLeft = paintballManager.getCooldown().get(player);

                float exp = (float) (1 - timeLeft / fireRate);

                exp = (float) ((exp > 1.0) ? 1.0 : exp);

                player.setExp(exp);

                if (timeLeft <= 0) {
                    paintballManager.getCooldown().remove(player);
                    player.setExp(0);
                    cancel();
                }

                paintballManager.getCooldown().replace(player, timeLeft - 1);
            }
        };

        runnable.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        Arena arena = this.arenaManager.getPlayerArena(player);

        if (arena == null)
            return;

        arena.spawnPlayer(player);

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = (Player) event.getEntity();

        if (p == null)
            return;

        Arena a = plugin.getArenaManager().getPlayerArena(p);

        if (a == null)
            return;

        event.getDrops().clear();
    }

    @EventHandler
    public void onEntityDamange(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            Arena arena = this.arenaManager.getPlayerArena(player);

            if (arena == null)
                return;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (event.getItem() == null)
                return;

            Player player = event.getPlayer();

            if (paintballManager.getCooldown().containsKey(player))
                return;

            for (Gun gun : paintballManager.getGuns()) {
                ItemStack eventItem = event.getItem();
                ItemStack gunItem = gun.getInGameItem();
                boolean typeEquals = eventItem.getType().equals(gunItem.getType());
                boolean nameEquals = isNamedItem(eventItem, gunItem.getItemMeta().getDisplayName());
                if (typeEquals && nameEquals) {
                    paintballManager.getCooldown().put(player, gun.getCooldown());
                    gun.fire(event.getPlayer());
                    runTimer(event.getPlayer(), (float) gun.getCooldown());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof Snowball) {
            if (paintballManager.getProjectiles().containsKey(projectile.getEntityId())) {
                paintballManager.getProjectiles().get(projectile.getEntityId()).cancel();
            }
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();

        Arena arena = this.arenaManager.getPlayerArena(player);

        if (arena == null)
            return;
        else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void lobbyEvents(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() == null)
                return;

            if (event.getItem().getType().equals(Material.WHITE_BED)
                    && isNamedItem(event.getItem(), plugin.getLanguageManager().getMessage("In-Game.Leave-Bed"))) {
                final Player player = event.getPlayer();

                Bukkit.dispatchCommand(player, "pb leave");
                event.setCancelled(true);
            } else {
                Player player = event.getPlayer();
                Arena arena = this.arenaManager.getPlayerArena(player);
                ItemStack eventItem = event.getItem();

                if (eventItem == null) {
                    System.out.println("Item not found");
                    return;
                }

                if (arena == null)
                    return;

                for (Gun gun : this.paintballManager.getGuns()) {
                    ItemStack gunItem = gun.getLobbyItem();
                    if (gunItem == null)
                        continue;
                    boolean typeEquals = eventItem.getType().equals(gunItem.getType());
                    boolean nameEquals = isNamedItem(eventItem, gunItem.getItemMeta().getDisplayName());
                    if (nameEquals && typeEquals) {
                        arena.setGunKit(player, gun);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Player shooter;
        Player hit;

//        if (event.getHitEntity() instanceof LivingEntity){
//        	LivingEntity ent = (LivingEntity) event.getHitEntity();
//        	ent.damage(100);
//        }

        if (event.getHitEntity() instanceof Player && event.getEntity().getShooter() instanceof Player) {
            shooter = (Player) event.getEntity().getShooter();
            hit = (Player) event.getHitEntity();
        } else {
            return;
        }

        if (shooter != null && hit != null) {
            Arena shooterA = arenaManager.getPlayerArena(shooter);
            Arena hitA = arenaManager.getPlayerArena(hit);

            if (shooter.getInventory().getItemInMainHand().getItemMeta() == null)
                return;

            if (shooter.hasPermission("paintball.admin") && shooter.getInventory().getItemInMainHand().getItemMeta()
                    .getDisplayName().equals(ChatColor.AQUA + "Admin Gun")) {
                if (hitA != null) {
                    hit.getWorld().strikeLightning(hit.getLocation());
                    Team hitTeam = hitA.getPlayerTeam(hit);

                    if (hitTeam == null)
                        return;

                    hit.teleport(hitTeam.getRandomLocation());
                    hit.playSound(hit.getLocation(), Sound.ITEM_SHIELD_BREAK, 2, 0.5f);

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            shooter.sendMessage("You owned " + hit.getName());
                            for (UUID id : shooterA.getPlayers()) {
                                Player p = Bukkit.getServer().getPlayer(id);
                                p.sendMessage(
                                        ChatColor.YELLOW + hit.getName() + ChatColor.GREEN + " has been " + "owned!");
                            }
                        }
                    });
                }
            }

            if (shooterA == null || hitA == null) {
                return;
            }

            if (shooterA.equals(hitA) && shooterA.getArenaState().equals(ArenaState.IN_GAME)) {
                Team shooterTeam = shooterA.getPlayerTeam(shooter);
                Team hitTeam = shooterA.getPlayerTeam(hit);
                if (!(shooterTeam.equals(hitTeam))) {
                    if (hitA.getSpawnTimer().containsKey(hit.getUniqueId()))
                        return;

                    int prevKills = shooterA.getKills().get(shooter.getUniqueId());
                    shooterA.getKills().replace(shooter.getUniqueId(), prevKills + 1);
                    shooter.setLevel(prevKills + 1);

                    hitA.addSpawnTime(hit, spawnTime);
                    hit.teleport(hitTeam.getRandomLocation());
                    hit.playSound(hit.getLocation(), Sound.ITEM_SHIELD_BREAK, 2, 0.5f);

                    shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2, 1);

                    hitA.updateScoreboard();

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            ChatColor cs = shooterA.getPlayerTeam(shooter).equals(shooterA.getBlueTeam())
                                    ? ChatColor.BLUE
                                    : ChatColor.RED;
                            ChatColor ch = cs.equals(ChatColor.BLUE) ? ChatColor.RED : ChatColor.BLUE;
                            for (UUID id : shooterA.getPlayers()) {
                                Player p = Bukkit.getServer().getPlayer(id);
                                p.sendMessage(cs + shooter.getName() + ChatColor.GREEN + " "
                                        + plugin.getLanguageManager().getMessage("In-Game.Killed").toLowerCase() + " "
                                        + ch + hit.getName());
                            }
                        }
                    });
                }
            }
        }

    }

    @EventHandler
    public void onSnowHit(ProjectileHitEvent event) {
        if (event.getHitBlock() == null)
            return;

        if (!(event.getEntity() instanceof Snowball))
            return;

        Player player = (Player) event.getEntity().getShooter();
        Snowball ball = (Snowball) event.getEntity();

        if (player == null)
            return;

        for (Gun gun : this.paintballManager.getGuns()) {
            ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
            ItemMeta gm = gun.getInGameItem().getItemMeta();

            if (im == null || gm == null)
                continue;

            if (im.getDisplayName().equals(gm.getDisplayName())) {
                gun.onHit(player, ball);
                break;
            }
        }

    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        if (this.arenaManager.getPlayerArena(p) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChangeEvent(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission("paintball.admin"))
            return;

        if (event.getLine(0).equalsIgnoreCase("pb join")) {
            if (event.getLine(1) == null)
                return;

            Arena a = this.arenaManager.getArenaByTitle(event.getLine(1));
            if (a == null) {
                event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Found")
                        .replace("%title%", event.getLine(1)));
                event.setCancelled(true);
            } else {
                event.getPlayer()
                        .sendMessage(ChatColor.GREEN + "Added sign for arena " + ChatColor.AQUA + a.getTitle());
                a.getSigns().add((Sign) event.getBlock().getState());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        a.updateSigns();
                    }
                }.runTaskLater(this.plugin, 10);
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        if (b.getState() instanceof Sign) {

            Sign sign = (Sign) b.getState();
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[PaintBall]")) {

                Arena a = this.arenaManager.getArenaByTitle(ChatColor.stripColor(sign.getLine(1)));
                if (a == null) {
                    p.sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Found").replace("%title%",
                            ChatColor.stripColor(sign.getLine(1))));
                } else {
                    if (!a.getArenaState().equals(ArenaState.IN_GAME)) {
                        Bukkit.dispatchCommand(p, "pb join " + a.getTitle());
                    } else {
                        Bukkit.dispatchCommand(p, "pb spectate " + a.getTitle());
                    }

                }
            }
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        Arena a = this.arenaManager.getPlayerArena(p);
        if (a != null)
            this.arenaManager.removePlayerFromArena(p);
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        TeleportCause tc = event.getCause();
        
        Arena a = arenaManager.getPlayerArena(p);
        
        if(a == null) a = arenaManager.getSpectatorArena(p);
       
        if(a != null)
            if(a.getArenaState().equals(ArenaState.IN_GAME) && tc.equals(TeleportCause.COMMAND)) {
                event.setCancelled(true);
                p.sendMessage(plugin.getLanguageManager().getMessage("Arena.Teleport-Not-Allowed"));
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

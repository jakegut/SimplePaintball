package com.borgdude.paintball.events;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.ArenaManager;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.ArenaState;
import com.borgdude.paintball.objects.GunKit;
import com.borgdude.paintball.objects.Team;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.util.ArrayList;
import java.util.UUID;

public class EventClass implements Listener {

    private ArenaManager arenaManager = Main.arenaManager;
    private Main plugin = Main.plugin;
    private HashMap<Player, Integer> cooldown;
    private Map<Integer, BukkitTask> projectiles = new HashMap<Integer, BukkitTask>();

    public EventClass(){
        super();
        cooldown = new HashMap<>();
    }
    
    public void runTimer(final Player player, float fireRate) {
    	BukkitRunnable runnable = new BukkitRunnable() {
        	@Override
            public void run() {
                int timeLeft = cooldown.get(player);
                
                float exp = (float) (1 - timeLeft / fireRate);
                
                exp = (float) ((exp > 1.0) ? 1.0 : exp);
                
                player.setExp(exp);
          	  
                if(timeLeft <= 0) {
              	  cooldown.remove(player);
              	  player.setExp(0);
              	  cancel();
                }
                
          	  cooldown.replace(player, timeLeft - 1);
    		}
        };
        
        runnable.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(event.getItem() != null && event.getItem().getType().equals(Material.GOLD_HOE)){
                if(isNamedItem(event.getItem(), ChatColor.GOLD + "PaintBall Sniper")){
                    final Player player = event.getPlayer();
                    
                    if(cooldown.containsKey(player)) return;
                    
                	final float fireRate = GunKit.LAUNCHER.getFireRate();
                	
                	cooldown.put(player, (int)fireRate);
                    
                    final Snowball snowball = player.launchProjectile(Snowball.class);
                    final Vector velocity = player.getLocation().getDirection().multiply(2);//set the velocity variable
                    snowball.setVelocity(velocity);
                    
                    projectiles.put(snowball.getEntityId(), new BukkitRunnable() {
                    	 
                        @Override
                        public void run() {
                            snowball.setVelocity(velocity);
                        }
                    }.runTaskTimer(plugin, 1, 1));
                    
                    player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
                    
                    
                    
                    runTimer(player, fireRate);

                }
            }else if(event.getItem() != null && event.getItem().getType().equals(Material.IRON_HOE) &&
                    isNamedItem(event.getItem(), ChatColor.GRAY + "PaintBall Shotgun")){
            	final Player player = event.getPlayer();
                
                if(cooldown.containsKey(player)) return;
                
            	final float fireRate = GunKit.SHOTGUN.getFireRate();
            	
            	cooldown.put(player, (int)fireRate);
            	
            	float accuracy = 0.15F;
                
                Snowball snowball = null;
                Vector velocity = null;
                
                for (int i = 0; i < 3; i++) { //Set i to 0 and as long as it is less than 5 add one to it then run the loop
                    snowball = player.launchProjectile(Snowball.class); //set the snowball variable
                    velocity = player.getLocation().getDirection().multiply(1.2); //set the velocity variable
                    velocity.add(getRandomVector(accuracy)); //set it's modified velocity
//                    velocity.add(new Vector(0.25, 0.12, 0.25));
                    snowball.setVelocity(velocity); //set the snowball's new velocity
//                        System.out.print(velocity.toString());
                }
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
                
                runTimer(player, fireRate);
            } else if(event.getItem() != null && event.getItem().getType().equals(Material.STONE_HOE) &&
                    isNamedItem(event.getItem(), ChatColor.LIGHT_PURPLE + "PaintBall MiniGun")){
final Player player = event.getPlayer();
                
                if(cooldown.containsKey(player)) return;
                
            	final float fireRate = GunKit.MINIGUN.getFireRate();
            	
            	cooldown.put(player, (int)fireRate);
            	
            	float accuracy = 0.3F;
                
                Snowball snowball = null;
                Vector velocity = null;
                
                snowball = player.launchProjectile(Snowball.class); //set the snowball variable
                velocity = player.getLocation().getDirection().multiply(0.9); //set the velocity variable
                velocity.add(getRandomVector(accuracy)); //set it's modified velocity
//                velocity.add(new Vector(0.25, 0.12, 0.25));
                snowball.setVelocity(velocity);
                
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
                
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Snowball snowball = player.launchProjectile(Snowball.class); //set the snowball variable
		                Vector velocity = player.getLocation().getDirection().multiply(0.9); //set the velocity variable
		                velocity.add(getRandomVector(accuracy)); //set it's modified velocity
//		                velocity.add(new Vector(0.25, 0.12, 0.25));
		                snowball.setVelocity(velocity);
		                
		                player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f); 
					}
                	
                }, 2L);

                runTimer(player, fireRate);
            	
            	
            }else if(event.getItem() != null && event.getItem().getType().equals(Material.IRON_SPADE) &&
                    isNamedItem(event.getItem(), ChatColor.DARK_GREEN + "PaintBall Rocket Launcher")){
                final Player player = event.getPlayer();
                
                if(cooldown.containsKey(player)) return;
                
            	final float fireRate = GunKit.LAUNCHER.getFireRate();
            	
            	cooldown.put(player, (int)fireRate);
                
                Snowball snowball = null;
                Vector velocity = null;
                
                snowball = player.launchProjectile(Snowball.class); //set the snowball variable
                velocity = player.getLocation().getDirection();//set the velocity variable
//                velocity.add(new Vector(0.25, 0.12, 0.25));
                snowball.setVelocity(velocity);
                
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
                
                
                
                runTimer(player, fireRate);

            }else if(event.getItem() != null && event.getItem().getType().equals(Material.DIAMOND_HOE) &&
                    isNamedItem(event.getItem(), ChatColor.AQUA + "Admin Gun")){
            
            	
                final Player player = event.getPlayer();
                
                if(!player.hasPermission("paintball.admin")) return;
                
                if(cooldown.containsKey(player)) return;
                
            	final float fireRate = GunKit.ADMIN.getFireRate();
            	
            	cooldown.put(player, (int)fireRate);
                
                final Snowball snowball = player.launchProjectile(Snowball.class);
                final Vector velocity = player.getLocation().getDirection().multiply(2);//set the velocity variable
                snowball.setVelocity(velocity);
                
                projectiles.put(snowball.getEntityId(), new BukkitRunnable() {
                	 
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
		                final Vector velocity = player.getLocation().getDirection().multiply(2);//set the velocity variable
		                snowball.setVelocity(velocity);
		                
		                projectiles.put(snowball.getEntityId(), new BukkitRunnable() {
		                	 
		                    @Override
		                    public void run() {
		                        snowball.setVelocity(velocity);
		                    }
		                }.runTaskTimer(plugin, 1, 1));
		                
		                player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f); 
					}
                	
                }, 2L);
                
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0.5f);
                
                
                
                runTimer(player, fireRate);

            }
        }
    }
    
    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if(projectile instanceof Snowball) {
            if(projectiles.containsKey(projectile.getEntityId())) {
                projectiles.get(projectile.getEntityId()).cancel();
            }
        }
    }
    
    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
    	if(!(event.getWhoClicked() instanceof Player)) return;
    	
    	Player player = (Player) event.getWhoClicked();
    	
    	Arena arena = this.arenaManager.getPlayerArena(player);
    	
    	if(arena == null) return;
    	else {
    		event.setCancelled(true);
    	}
    }
    
    public Vector getRandomVector(float accuracy) {
    	return new Vector(getRandomComp(accuracy), getRandomComp(accuracy), getRandomComp(accuracy));
    }
    
    public float getRandomComp(float accuracy) {
    	return (float) (Math.random() * (accuracy * 2) - accuracy);
    }
    
    @EventHandler
    public void lobbyEvents(PlayerInteractEvent event) {
    	if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
    		if(event.getItem() == null) return;
    		
    		if(event.getItem().getType().equals(Material.BED) &&
                    isNamedItem(event.getItem(), ChatColor.AQUA + "Leave Arena")){
	        	final Player player = event.getPlayer();
	        	
	        	Bukkit.dispatchCommand(player, "pb leave");
            } else if (event.getItem().getType().equals(Material.WOOL)){
            	Player player = event.getPlayer();
            	Arena arena = this.arenaManager.getPlayerArena(player);
            	
            	if (arena == null) return;
            	
            	if(isNamedItem(event.getItem(), ChatColor.YELLOW + GunKit.REGULAR.getFormattedName())) {
            		arena.setGunKit(player, GunKit.REGULAR);
            	} else if(isNamedItem(event.getItem(), ChatColor.GRAY + GunKit.SHOTGUN.getFormattedName())) {
            		arena.setGunKit(player, GunKit.SHOTGUN);
            	} else if(isNamedItem(event.getItem(), ChatColor.LIGHT_PURPLE + GunKit.MINIGUN.getFormattedName())) {
            		arena.setGunKit(player, GunKit.MINIGUN);
            	} else if(isNamedItem(event.getItem(), ChatColor.DARK_GREEN + GunKit.LAUNCHER.getFormattedName())) {
            		arena.setGunKit(player, GunKit.LAUNCHER);
            	}
            }
    	}
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event){
        Player shooter;
        Player hit;
        
//        if (event.getHitEntity() instanceof LivingEntity){
//        	LivingEntity ent = (LivingEntity) event.getHitEntity();
//        	ent.damage(100);
//        }
        
        if(event.getHitEntity() instanceof Player && event.getEntity().getShooter() instanceof Player){
            shooter = (Player) event.getEntity().getShooter();
            hit = (Player) event.getHitEntity();
        } else {
            return;
        }

        if(shooter != null && hit != null && shooter instanceof Player && hit instanceof Player){
            Arena shooterA = arenaManager.getPlayerArena(shooter);
            Arena hitA = arenaManager.getPlayerArena(hit);

            if (shooter.hasPermission("paintball.admin") && shooter.getInventory().getItemInMainHand().
                    getItemMeta().getDisplayName().equals(ChatColor.AQUA + "Admin Gun")){
                if (hitA != null){
                    hit.getWorld().strikeLightning(hit.getLocation());
                    Team hitTeam = hitA.getPlayerTeam(hit);
                    hit.teleport(hitTeam.getRandomLocation());
                    hit.playSound(hit.getLocation(), Sound.ITEM_SHIELD_BREAK, 2, 0.5f);

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            shooter.sendMessage("You owned " + hit.getName());
                            for(UUID id : shooterA.getPlayers()){
                                Player p = Bukkit.getServer().getPlayer(id);
                                p.sendMessage(ChatColor.YELLOW + hit.getName() + ChatColor.GREEN + " has been " +
                                        "owned!");
                            }
                        }
                    });
                }
            }


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

                    hit.teleport(hitTeam.getRandomLocation());
                    hit.playSound(hit.getLocation(), Sound.ITEM_SHIELD_BREAK, 2, 0.5f);

                    hitA.updateScoreboard();

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
    public void onSnowHit(ProjectileHitEvent event) {
    	if(event.getHitBlock() == null) return;
    	
    	if(!(event.getEntity() instanceof Snowball)) return;
    	
    	Player player = (Player) event.getEntity().getShooter();
    	Snowball ball = (Snowball) event.getEntity();
    	
    	if (ball.hasMetadata("fired")) return;
    	
    	if(player == null) return;
    	
    	
		 if (player.getInventory().getItemInMainHand()
				 .getItemMeta().getDisplayName().equals(ChatColor.DARK_GREEN + "PaintBall Rocket Launcher")){
			 
			 Location spawnLocation = ball.getLocation();
			 
			 for(int i = 0; i < 8; i++) {
				 Snowball snowball = player.getWorld().spawn(spawnLocation, Snowball.class);
				 double vecX = Math.cos(Math.toRadians(i * 45));
				 double vecY = 1;
				 double vecZ = Math.sin(Math.toRadians(i * 45));
	    		 snowball.setVelocity(new Vector(vecX, vecY, vecZ).multiply(0.35D).add(getRandomVector(0.2f)));
	    		 snowball.setShooter(player);
	    		 snowball.setMetadata("fired", new FixedMetadataValue(plugin, new Boolean(true)));
			 }
 		 
//    		 Snowball snowball = player.getWorld().spawn(block.getLocation().add(0, 1, 0), Snowball.class);
//    		 snowball.setVelocity(new Vector(1, 1, 1).multiply(0.5D));
//    		 snowball.setShooter(player);   		 
    		 
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        Arena a = this.arenaManager.getPlayerArena(p);
        if(a != null){
            this.arenaManager.removePlayerFromArena(p);
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

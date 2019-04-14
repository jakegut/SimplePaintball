package com.borgdude.paintball.commands;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.ArenaManager;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.ArenaState;

import net.md_5.bungee.api.ChatColor;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaintballCommand implements CommandExecutor {

    private ArenaManager arenaManager = Main.arenaManager;
    
    private void sendHelpCommand(Player p, String command, String description) {
    	p.sendMessage(ChatColor.GREEN + command + ChatColor.BLUE + " - " + description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;

            if(command.getName().equalsIgnoreCase("pb")){
            	if(args.length <= 0) {
            		Bukkit.dispatchCommand(player, "pb help");
            		return true;
            	}
                if(player.hasPermission("paintball.admin")){
                    if(args[0].equalsIgnoreCase("create")){
                        if(args.length < 2 || args[1].length() < 2){
                            player.sendMessage(ChatColor.RED + "Usage: /pb create <title>");
                            return true;
                        }

                        String title = args[1];

                        Arena a = this.arenaManager.createArena(player, title);

                        player.sendMessage(ChatColor.GREEN + "Created pb arena: " + ChatColor.AQUA + a.getTitle());
                    } else if (args[0].equalsIgnoreCase("edit")){

                        if(args.length < 2 || args[1].length() < 2){
                            player.sendMessage(ChatColor.RED + "Usage: /pb edit <title>");
                            return true;
                        }

                        String title = args[1];

                        Arena a = this.arenaManager.getArenaByTitle(title);

                        if(a != null){
                            this.arenaManager.setCurrentlyEditing(player, a);
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Could not find arena with title " + ChatColor.YELLOW + title);
                            return true;
                        }
                    } else if (args[0].equalsIgnoreCase("set")){

                        Arena a = this.arenaManager.getCurrentlyEditing(player);

                        if(a == null){
                            player.sendMessage(ChatColor.RED + "You need to be editing an arena: /pb edit <title>");
                            return true;
                        }


                        if(args.length < 2 || args[1].length() < 2){
                            player.sendMessage(ChatColor.RED + "Usage: /pb set <option> <add.>");
                            return true;
                        }

                        if(args[1].equalsIgnoreCase("max")){
                            if(args.length < 3)
                                return false;

                            int max = Integer.valueOf(args[2]);

                            if(max < a.getMinPlayers()){
                                player.sendMessage(ChatColor.RED + "Invalid number...try again");
                                return true;
                            } else {
                                a.setMaxPlayers(max);
                                player.sendMessage(ChatColor.GREEN + "Set max number to: " + max + " for arena: " +
                                        a.getTitle());
                                a.updateSigns();
                                return true;
                            }
                        } else if(args[1].equalsIgnoreCase("min")){
                            if(args.length < 3)
                                return false;

                            int min = Integer.valueOf(args[2]);

                            if(min < 2 || min > a.getMaxPlayers()){
                                player.sendMessage(ChatColor.RED + "Invalid number...try again");
                                return true;
                            } else {
                                a.setMinPlayers(min);
                                player.sendMessage(ChatColor.GREEN + "Set min number to: " + min + " for arena: " +
                                        a.getTitle());
                                return true;
                            }
                        } else if(args[1].equalsIgnoreCase("blue")){
                            Location loc = player.getLocation();
                            a.getBlueTeam().addLocation(loc);
                            player.sendMessage(ChatColor.GREEN + "Added spawn to " + ChatColor.BLUE + "blue" +
                                    ChatColor.GREEN + "team. Spawn count: " + ChatColor.AQUA +
                                    a.getBlueTeam().getSpawnLocations().size());
                            return true;
                        } else if(args[1].equalsIgnoreCase("red")){
                            Location loc = player.getLocation();
                            a.getRedTeam().addLocation(loc);
                            player.sendMessage(ChatColor.GREEN + "Added spawn to " + ChatColor.RED + "red" +
                                    ChatColor.GREEN + "team. Spawn count: " + ChatColor.AQUA +
                                    a.getRedTeam().getSpawnLocations().size());
                            return true;
                        } else if(args[1].equalsIgnoreCase("lobby")){
                            Location loc = player.getLocation();
                            a.setLobbyLocation(loc);
                            player.sendMessage(ChatColor.GREEN + "Changed lobby location for: " + ChatColor.AQUA +
                                    a.getTitle());
                            return true;
                        } else if(args[1].equalsIgnoreCase("end")){
                            Location loc = player.getLocation();
                            a.setEndLocation(loc);
                            player.sendMessage(ChatColor.GREEN + "Changed end location for: " + ChatColor.AQUA +
                                    a.getTitle());
                            return true;
                        } else if(args[1].equalsIgnoreCase("activate")){
                            a.setActivated(player);
                            return true;
                        }
                    } else if (args[0].equalsIgnoreCase("reset")) {
                    	Arena a = this.arenaManager.getCurrentlyEditing(player);

                        if(a == null){
                            player.sendMessage(ChatColor.RED + "You need to be editing an arena: /pb edit <title>");
                            return true;
                        }


                        if(args.length < 2 || args[1].length() < 2){
                            player.sendMessage(ChatColor.RED + "Usage: /pb reset <blue | red>");
                            return true;
                        }
                        
                        String changedTeam = "?????";
                        
                        if(args[1].equalsIgnoreCase("red")) {
                        	a.getRedTeam().getSpawnLocations().clear();
                        	
                        	changedTeam = ChatColor.RED + "Red Team";
                        } else if(args[1].equalsIgnoreCase("blue")){
                            a.getBlueTeam().getSpawnLocations().clear();
                            
                            changedTeam = ChatColor.BLUE + "Blue Team";
                        }
                        
                        a.setActivated(false);
                        player.sendMessage(ChatColor.GREEN + "The spawn locations for " + changedTeam +
                    			ChatColor.GREEN + " have been cleared and the arena has been " + ChatColor.YELLOW + "deactivated." +
                    			ChatColor.GREEN + "Please add blue/red spawns and run " + ChatColor.YELLOW + "/pb set activate " + ChatColor.GREEN + "when ready");
                        return true;
                    } else if (args[0].equalsIgnoreCase("start")) {
                    	
                    	Arena a = null;
                    	if(args.length < 2) {
                    		a = this.arenaManager.getPlayerArena(player);
                    		if(a == null) {
                    			player.sendMessage(ChatColor.RED + "You're not in an arena, please join an arena or specify an arena (/pb start [title]");
                    			return true;
                    		}
                    	} else {
                    		a = this.arenaManager.getArenaByTitle(args[1]);
                    		if(a == null) {
                    			player.sendMessage(ChatColor.RED + "Arena " + ChatColor.YELLOW + args[1] + ChatColor.RED + " not found.");
                    			return true;
                    		}
                    	}
                    	
                    	if(a.getArenaState().equals(ArenaState.STARTING)) {
                			if(a.getTimer() > 5)
                				a.setTimer(5);
                		} else {
                			player.sendMessage(ChatColor.RED + "Arena needs to be in a state of starting to force start.");
                		}
                		return true;
                    } else if (args[0].equalsIgnoreCase("reload")) {
                    	player.sendMessage(ChatColor.YELLOW + "Starting reload...");
                    	Main.plugin.reloadConfig();
                    	arenaManager.getArenas();
                    	arenaManager.saveArenas();
                    	player.sendMessage(ChatColor.YELLOW + "Reload finished.");
                    	return true;
                    }
                }

                if(args[0].equalsIgnoreCase("join")){
                    if(args.length < 2 || args[1].length() < 2){
                        player.sendMessage(ChatColor.RED + "Usage: /pb join <title>");
                        return true;
                    }

                    Arena a = this.arenaManager.getArenaByTitle(args[1]);

                    if (a == null) {
                        player.sendMessage(ChatColor.RED + "No arena found with the name: " + ChatColor.YELLOW +
                                args[1]);
                        return true;
                    } else if (!a.isActivated()){
                        player.sendMessage(ChatColor.RED + "This arena is not activated: " + ChatColor.YELLOW +
                                args[1]);
                        return true;
                    } else {
                        try {
							this.arenaManager.addPlayerToArena(player, a);
						} catch (IOException e) {
							e.printStackTrace();
						}
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("leave")){
                    try {
						this.arenaManager.removePlayerFromArena(player);
					} catch (IOException e) {
						e.printStackTrace();
					}
                    return true;
                } else if (args[0].equalsIgnoreCase("list")){
                    if(arenaManager.getArena().size() == 0){
                        player.sendMessage(ChatColor.RED + "There are no arenas set up yet!");
                        return true;
                    }

                    player.sendMessage(ChatColor.GREEN + "List of activated arenas:");
                    int idx = 1;
                    for(Arena a : arenaManager.getArena()){
                        if(a.isActivated()){
                            player.sendMessage(ChatColor.GREEN + Integer.toString(idx) + ". " + ChatColor.AQUA +
                                    a.getTitle());
                            idx++;
                        }
                    }
                } else if (args[0].equalsIgnoreCase("spectate")) {
                	if(args.length < 2 || args[1].length() < 2){
                        player.sendMessage(ChatColor.RED + "Usage: /pb spectate <title>");
                        return true;
                    }
                	
                	Arena a = this.arenaManager.getArenaByTitle(args[1]);

                    if (a == null) {
                        player.sendMessage(ChatColor.RED + "No arena found with the name: " + ChatColor.YELLOW +
                                args[1]);
                        return true;
                    } else if (!a.isActivated()){
                        player.sendMessage(ChatColor.RED + "This arena is not activated: " + ChatColor.YELLOW +
                                args[1]);
                        return true;
                    } else if (!a.getArenaState().equals(ArenaState.IN_GAME)) {
                    	player.sendMessage(ChatColor.RED + "This arena is not in game: " + ChatColor.YELLOW +
                                args[1]);
                    	return true;
                    }
                    Main.arenaManager.addSpectatorToArena(player, a);
                    return true;
                } else if (args[0].equalsIgnoreCase("help")) {
                	player.sendMessage(ChatColor.YELLOW + "--- Simple Paintball Commands ---");
                	sendHelpCommand(player, "/pb join <title>", "Join an arena with a given title");
                	sendHelpCommand(player, "/pb spectate <title>", "Spectate an arena that's in-game");
                	sendHelpCommand(player, "/pb list", "A list of all of the arenas you're able to join or spectate");
                	if(player.hasPermission("paintball.admin")) {
                		player.sendMessage(ChatColor.RED + "Admin Commands ---");
                		sendHelpCommand(player, "/pb create <title>", "Create an arena with a given title");
                		sendHelpCommand(player, "/pb edit <title>", "Edit an arena with a given title");
                		sendHelpCommand(player, "/pb set <red | blue>", "Add a blue or red spawn to your currently editing arena");
                		sendHelpCommand(player, "/pb set <min | max>", "Set the minimum or maximum number of players of an arena");
                		sendHelpCommand(player, "/pb set end", "Set the location of where to teleport when the game ends");
                		sendHelpCommand(player, "/pb set lobby", "Set the location of where to teleport when players wait for the game to start");
                		sendHelpCommand(player, "/pb set activate", "Activate the arena for players to join, must have set all teleport locations first");
                		sendHelpCommand(player, "/pb reset <red | blue>", "Reset the locations of the red/blue spawns. This will deactivate the arean");
                	}
                	return true;
                }

            }
        }
        return false;
    }
}

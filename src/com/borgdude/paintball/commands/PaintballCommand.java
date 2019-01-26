package com.borgdude.paintball.commands;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.ArenaManager;
import com.borgdude.paintball.objects.Arena;
import net.md_5.bungee.api.ChatColor;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaintballCommand implements CommandExecutor {

    private ArenaManager arenaManager = Main.arenaManager;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;

            if(command.getName().equalsIgnoreCase("pb")){
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

                            if(max < a.getMaxPlayers()){
                                player.sendMessage(ChatColor.RED + "Invalid number...try again");
                                return true;
                            } else {
                                a.setMaxPlayers(max);
                                player.sendMessage(ChatColor.GREEN + "Set max number to: " + max + "For arena: " +
                                        a.getTitle());
                                return true;
                            }
                        } else if(args[1].equalsIgnoreCase("min")){
                            if(args.length < 3)
                                return false;

                            int min = Integer.valueOf(args[2]);

                            if(min < 2){
                                player.sendMessage(ChatColor.RED + "Invalid number...try again");
                                return true;
                            } else {
                                a.setMinPlayers(min);
                                player.sendMessage(ChatColor.GREEN + "Set min number to: " + min + "For arena: " +
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
                }

            }
        }
        return false;
    }
}

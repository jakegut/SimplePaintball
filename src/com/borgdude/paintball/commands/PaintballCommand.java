package com.borgdude.paintball.commands;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.database.PlayerStats;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.ArenaState;
import com.borgdude.paintball.objects.Team;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaintballCommand implements CommandExecutor {

    private final Main plugin;

    public PaintballCommand(Main plugin) {
        this.plugin = plugin;
    }

    private void sendHelpCommand(Player p, String command, String description) {
        p.sendMessage(ChatColor.GREEN + command + " - " + description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (command.getName().equalsIgnoreCase("pb")) {
                if (args.length <= 0) {
                    Bukkit.dispatchCommand(player, "pb help");
                    return true;
                }
                if (player.hasPermission("paintball.admin")) {
                    if (args[0].equalsIgnoreCase("create")) {
                        if (args.length < 2 || args[1].length() < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /pb create <title>");
                            return true;
                        }

                        String title = args[1];

                        Arena a = plugin.getArenaManager().createArena(player, title);

                        String msg = plugin.getLanguageManager().getMessage("Edit.Created-Arena").replace("%title%",
                                a.getTitle());
                        player.sendMessage(msg);

                        return true;
                    } else if (args[0].equals("remove")) {
                        if (args.length < 2 || args[1].length() < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /pb remove <title>");
                            return true;
                        }

                        String title = args[1];

                        player.sendMessage(plugin.getArenaManager().removeArena(title));

                        return true;
                    } else if (args[0].equalsIgnoreCase("edit")) {

                        if (args.length < 2 || args[1].length() < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /pb edit <title>");
                            return true;
                        }

                        String title = args[1];

                        Arena a = plugin.getArenaManager().getArenaByTitle(title);

                        if (a != null) {
                            plugin.getArenaManager().setCurrentlyEditing(player, a);
                            return true;
                        } else {
                            player.sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Found")
                                    .replace("%title%", title));
                            return true;
                        }
                    } else if (args[0].equalsIgnoreCase("set")) {

                        Arena a = plugin.getArenaManager().getCurrentlyEditing(player);

                        if (a == null) {
                            player.sendMessage(ChatColor.RED + "You need to be editing an arena: /pb edit <title>");
                            return true;
                        }

                        if (args.length < 2 || args[1].length() < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /pb set <option> <add.>");
                            return true;
                        }

                        if (args[1].equalsIgnoreCase("max")) {
                            if (args.length < 3)
                                return false;

                            int max = Integer.valueOf(args[2]);

                            if (max < a.getMinPlayers()) {
                                player.sendMessage(ChatColor.RED + "Invalid number...try again");
                                return true;
                            } else {
                                a.setMaxPlayers(max);
                                player.sendMessage(
                                        ChatColor.GREEN + "Set max number to: " + max + " for arena: " + a.getTitle());
                                a.updateSigns();
                                return true;
                            }
                        } else if (args[1].equalsIgnoreCase("min")) {
                            if (args.length < 3)
                                return false;

                            int min = Integer.valueOf(args[2]);

                            if (min < 2 || min > a.getMaxPlayers()) {
                                player.sendMessage(ChatColor.RED + "Invalid number...try again");
                                return true;
                            } else {
                                a.setMinPlayers(min);
                                player.sendMessage(
                                        ChatColor.GREEN + "Set min number to: " + min + " for arena: " + a.getTitle());
                                return true;
                            }
//                        } else if (args[1].equalsIgnoreCase("blue")) {
//                            Location loc = player.getLocation();
//                            a.getBlueTeam().addLocation(loc);
//                            player.sendMessage(ChatColor.GREEN + "Added spawn to " + ChatColor.BLUE + "blue"
//                                    + ChatColor.GREEN + "team. Spawn count: " + ChatColor.AQUA
//                                    + a.getBlueTeam().getSpawnLocations().size());
//                            return true;
//                        } else if (args[1].equalsIgnoreCase("red")) {
//                            Location loc = player.getLocation();
//                            a.getRedTeam().addLocation(loc);
//                            player.sendMessage(ChatColor.GREEN + "Added spawn to " + ChatColor.RED + "red"
//                                    + ChatColor.GREEN + "team. Spawn count: " + ChatColor.AQUA
//                                    + a.getRedTeam().getSpawnLocations().size());
//                            return true;
                        } else if (args[1].equalsIgnoreCase("lobby")) {
                            Location loc = player.getLocation();
                            a.setLobbyLocation(loc);
                            player.sendMessage(
                                    ChatColor.GREEN + "Changed lobby location for: " + ChatColor.AQUA + a.getTitle());
                            return true;
                        } else if (args[1].equalsIgnoreCase("end")) {
                            Location loc = player.getLocation();
                            a.setEndLocation(loc);
                            player.sendMessage(
                                    ChatColor.GREEN + "Changed end location for: " + ChatColor.AQUA + a.getTitle());
                            return true;
                        } else if (args[1].equalsIgnoreCase("activate")) {
                            a.setActivated(player);
                            plugin.getArenaManager().saveArenas();
                            return true;
                        } else {
                            Team t = a.getTeams().get(StringUtils.capitalize(args[1]));
                            if(t == null) {
                                player.sendMessage(ChatColor.RED + "Command not found. Usage: /pb set <option>");
                                return true;
                            }
                            
                            Location loc = player.getLocation();
                            t.addLocation(loc);
                            player.sendMessage(ChatColor.GREEN + "Added spawn for " + t.getChatName() + ChatColor.GREEN + " team. Spawn Count : " + ChatColor.AQUA + 
                                    t.getSpawnLocations().size());
                            return true;
                        }
                    } else if (args[0].equalsIgnoreCase("reset")) {
                        Arena a = plugin.getArenaManager().getCurrentlyEditing(player);

                        if (a == null) {
                            player.sendMessage(ChatColor.RED + "You need to be editing an arena: /pb edit <title>");
                            return true;
                        }
                        
                        if(a.getArenaState().equals(ArenaState.IN_GAME)) {
                            player.sendMessage(ChatColor.RED + "Arena is currently in game, please wait");
                        }

                        if (args.length < 2 || args[1].length() < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /pb reset <team>");
                            return true;
                        }

                        String changedTeam = "?????";

                        Team t = a.getTeams().get(StringUtils.capitalize(args[1]));
                        if(t == null) {
                            player.sendMessage(ChatColor.RED + "Team not found");
                            return true;
                        }
                        
                        t.getSpawnLocations().clear();
                        changedTeam = t.getChatName() + " Team";
                        
//                        if (args[1].equalsIgnoreCase("red")) {
//                            a.getRedTeam().getSpawnLocations().clear();
//
//                            changedTeam = ChatColor.RED + "Red Team";
//                        } else if (args[1].equalsIgnoreCase("blue")) {
//                            a.getBlueTeam().getSpawnLocations().clear();
//
//                            changedTeam = ChatColor.BLUE + "Blue Team";
//                        }

                        a.setActivated(false);
                        player.sendMessage(ChatColor.GREEN + "The spawn locations for " + changedTeam + ChatColor.GREEN
                                + " have been cleared and the arena has been " + ChatColor.YELLOW + "deactivated."
                                + ChatColor.GREEN + "Please add team spawns and run " + ChatColor.YELLOW
                                + "/pb set activate " + ChatColor.GREEN + "when ready");
                        return true;
                    } else if (args[0].equalsIgnoreCase("start")) {

                        Arena a = null;
                        if (args.length < 2) {
                            a = plugin.getArenaManager().getPlayerArena(player);
                            if (a == null) {
                                player.sendMessage(ChatColor.RED
                                        + "You're not in an arena, please join an arena or specify an arena (/pb start [title]");
                                return true;
                            }
                        } else {
                            a = plugin.getArenaManager().getArenaByTitle(args[1]);
                            if (a == null) {
                                player.sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Found")
                                        .replace("%title%", args[1]));
                                return true;
                            }
                        }

                        if (a.getArenaState().equals(ArenaState.STARTING)) {
                            if (a.getTimer() > 5)
                                a.setTimer(5);
                        } else {
                            player.sendMessage(
                                    ChatColor.RED + "Arena needs to be in a state of starting to force start.");
                        }
                        return true;
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        player.sendMessage(ChatColor.YELLOW + "Starting reload...");
                        Main.plugin.reloadConfig();
                        plugin.getArenaManager().getArenas();
                        plugin.getArenaManager().saveArenas();
                        player.sendMessage(ChatColor.YELLOW + "Reload finished.");
                        return true;
                    } else if (args[0].equalsIgnoreCase("info")) {
                        if (args.length < 2 || args[1].length() < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /pb info <title>");
                            return true;
                        }

                        Arena arena = plugin.getArenaManager().getArenaByTitle(args[1]);

                        if (arena == null) {
                            player.sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Found")
                                    .replace("%title%", args[1]));
                            return true;
                        }

                        sendArenaInfo(player, "Title", arena.getTitle());
                        sendArenaInfo(player, "Max Players", String.valueOf(arena.getMaxPlayers()));
                        sendArenaInfo(player, "Min Players", String.valueOf(arena.getMinPlayers()));
                        sendArenaInfo(player, "Number of signs", String.valueOf(arena.getSignLocations().size()));
                        String t = "";
                        for(Team team : arena.getTeams().values()) {
                            t += team.getChatColor() + "(" + team.getName() + ": " + team.getSpawnLocations().size() + ")  ";
                        }
                        sendArenaInfo(player, "Teams", t);
                        
                    } else if (args[0].equalsIgnoreCase("save")) {
                        plugin.getArenaManager().saveArenas();
                        player.sendMessage(plugin.getLanguageManager().getMessage("Edit.Force-Save"));
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("join")) {
                    if (args.length < 2 || args[1].length() < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /pb join <title>");
                        return true;
                    }

                    Arena a = plugin.getArenaManager().getArenaByTitle(args[1]);

                    if (a == null) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Found")
                                .replace("%title%", args[1]));
                        return true;
                    } else if (!a.isActivated()) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Activated")
                                .replace("%title%", args[1]));
                        return true;
                    } else {
                        try {
                            plugin.getArenaManager().addPlayerToArena(player, a);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("leave")) {
                    plugin.getArenaManager().removePlayerFromArena(player);
                    return true;
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (plugin.getArenaManager().getArena().size() == 0) {
                        player.sendMessage(ChatColor.RED + "There are no arenas set up yet!");
                        return true;
                    }

                    player.sendMessage(plugin.getLanguageManager().getMessage("List.Title"));
                    int idx = 1;
                    for (Arena a : plugin.getArenaManager().getActivatedArenas()) {
                        player.sendMessage(
                                ChatColor.GREEN + Integer.toString(idx) + ". " + ChatColor.AQUA + a.getTitle());
                        idx++;

                    }
                } else if (args[0].equalsIgnoreCase("spectate")) {
                    if (args.length < 2 || args[1].length() < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /pb spectate <title>");
                        return true;
                    }

                    Arena a = plugin.getArenaManager().getArenaByTitle(args[1]);

                    if (a == null) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Found")
                                .replace("%title%", args[1]));
                        return true;
                    } else if (!a.isActivated()) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("Edit.Arena-Not-Activated")
                                .replace("%title%", args[1]));
                        return true;
                    } else if (!a.getArenaState().equals(ArenaState.IN_GAME)) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("Spectate.Arena-Not-In-Game")
                                .replace("%title%", args[1]));
                        return true;
                    }
                    
                    if(plugin.getConfig().getBoolean("Arena-Spectate"))
                        plugin.getArenaManager().addSpectatorToArena(player, a);
                    return true;
                } else if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("lb")) {
                    if (!Main.plugin.getConfig().getBoolean("Stats.Track") || Main.db == null) {
                        player.sendMessage(plugin.getLanguageManager().getMessage("Command.Disabled"));
                        return true;
                    }

                    if (args.length < 2 || args[1].length() < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /pb leaderboard <wins | kills>");
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("wins")) {
                        player.sendMessage(
                                plugin.getLanguageManager().getMessage("Leaderboard.Title").replace("%stat%", "Wins"));
                        List<PlayerStats> stats = Main.db.getTopWins(10);
                        int i = 1;
                        for (PlayerStats stat : stats) {
                            player.sendMessage(ChatColor.GREEN + String.valueOf(i++) + ". " + ChatColor.BLUE + stat.name
                                    + ": " + ChatColor.GREEN + stat.wins + " wins");
                        }
                        return true;
                    } else if (args[1].equalsIgnoreCase("kills")) {
                        player.sendMessage(
                                plugin.getLanguageManager().getMessage("Leaderboard.Title").replace("%stat%", "Kills"));
                        List<PlayerStats> stats = Main.db.getTopKills(10);
                        int i = 1;
                        for (PlayerStats stat : stats) {
                            System.out.println(stat);
                            player.sendMessage(ChatColor.GREEN + String.valueOf(i++) + ". " + ChatColor.BLUE + stat.name
                                    + ": " + ChatColor.GREEN + stat.kills + " kills");
                        }
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "Usage: /pb leaderboard <wins | kills>");
                        return true;
                    }

                } else if (args[0].equalsIgnoreCase("help")) {
                    player.sendMessage(ChatColor.YELLOW + "--- Simple Paintball Commands ---");
                    sendHelpCommand(player, "/pb join <title>",
                            plugin.getLanguageManager().getMessage("Help.Basic.Join"));
                    sendHelpCommand(player, "/pb spectate <title>",
                            plugin.getLanguageManager().getMessage("Help.Basic.Spectate"));
                    sendHelpCommand(player, "/pb list", plugin.getLanguageManager().getMessage("Help.Basic.List"));
                    sendHelpCommand(player, "/pb leaderboard < kills | wins >",
                            plugin.getLanguageManager().getMessage("Help.Basic.Leaderboard"));
                    if (player.hasPermission("paintball.admin")) {
                        player.sendMessage(ChatColor.RED + "Admin Commands ---");
                        sendHelpCommand(player, "/pb create <title>", "Create an arena with a given title");
                        sendHelpCommand(player, "/pb edit <title>", "Edit an arena with a given title");
                        sendHelpCommand(player, "/pb set <red | blue>",
                                "Add a blue or red spawn to your currently editing arena");
                        sendHelpCommand(player, "/pb set <min | max>",
                                "Set the minimum or maximum number of players of an arena");
                        sendHelpCommand(player, "/pb set end",
                                "Set the location of where to teleport when the game ends");
                        sendHelpCommand(player, "/pb set lobby",
                                "Set the location of where to teleport when players wait for the game to start");
                        sendHelpCommand(player, "/pb set activate",
                                "Activate the arena for players to join, must have set all teleport locations first");
                        sendHelpCommand(player, "/pb reset <red | blue>",
                                "Reset the locations of the red/blue spawns. This will deactivate the arean");
                    }
                    return true;
                }

            }
        }
        return false;
    }

    private void sendArenaInfo(Player p, String type, String info) {
        p.sendMessage(ChatColor.BLUE + type + ": " + ChatColor.GREEN + info);
    }
}

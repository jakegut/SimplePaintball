package com.borgdude.paintball.managers;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.ArenaState;
import com.borgdude.paintball.objects.Gun;
import com.borgdude.paintball.objects.Team;
import com.borgdude.paintball.utils.LocationUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private ArrayList<Arena> arenas;
    private HashMap<UUID, Arena> currentlyEditing;
    private Main plugin;
    private PaintballManager paintballManager = Main.paintballManager;


    public ArenaManager(ArrayList<Arena> arenas, Main plugin) {
        this.arenas = arenas;
        this.plugin = plugin;
        this.currentlyEditing = new HashMap<>();
    }

    public ArrayList<Arena> getArena(){
        return arenas;
    }

    public void addPlayerToArena(Player player, Arena a) throws IOException{
        if(getPlayerArena(player) != null){
            player.sendMessage("You're already in an arena. Leave first: /pb leave");
            return;
        }

        if(a.getArenaState() == ArenaState.IN_GAME || a.getArenaState() == ArenaState.RESTARTING || a.getArenaState() == ArenaState.ENDING){
            player.sendMessage(ChatColor.YELLOW + "The arena is currently in a game, or something. Please wait");
            return;
        }

        player.teleport(a.getLobbyLocation());
        a.getPlayers().add(player.getUniqueId());
        Main.inventoryManager.saveInventory(player);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        a.setGunKit(player, paintballManager.getGunByName("Regular"));
        player.sendMessage(ChatColor.GREEN + "You have joined arena: " + ChatColor.AQUA + a.getTitle());
        player.setGameMode(GameMode.ADVENTURE);
        addLobbyItems(player);
        a.checkToStart();
    }

    private void addLobbyItems(Player player){
        ItemStack is = new ItemStack(Material.WHITE_BED);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.AQUA + "Leave Arena");
        is.setItemMeta(im);
        player.getInventory().setItem(2, is);
        
        int i = 3;
        for(Gun gun : paintballManager.getGuns()) {
        	ItemStack lobbyItem = gun.getLobbyItem();
        	if (lobbyItem == null) continue;
        	player.getInventory().setItem(i, lobbyItem);
        	i++;
        }
    }

    public void removePlayerFromArena(Player player) throws IOException{
        Arena a = getPlayerArena(player);

        if(a == null){
            player.sendMessage("You're not in an arena.");
            return;
        }

        if(a.getArenaState().equals(ArenaState.IN_GAME)){
            a.removePlayerInGame(player);
        } else {
            player.sendMessage(ChatColor.YELLOW + "You have left the arena.");
            player.teleport(a.getEndLocation());
            restorePlayerData(player);
            a.getPlayers().remove(player.getUniqueId());
            if(a.getBossBar() != null){
                a.getBossBar().removePlayer(player);
            }
            a.updateSigns();
            return;
        }

    }

    private void restorePlayerData(Player player) throws IOException {
    	player.getInventory().clear();
		Main.inventoryManager.restoreInventory(player);
	}

	public Arena getPlayerArena(Player player){
        UUID pUUID = player.getUniqueId();

        for(Arena a: arenas){
            for(UUID id : a.getPlayers()){
                if(id.equals(pUUID))
                    return a;
            }
        }

        return null;
    }

    public Arena getArenaByTitle(String title){
        for(Arena a : arenas){
            if(a.getTitle().equalsIgnoreCase(title))
                return a;
        }

        return null;
    }


    public Arena getCurrentlyEditing(Player player){
        if(currentlyEditing.containsKey(player.getUniqueId())){
            return currentlyEditing.get(player.getUniqueId());
        }

        return null;
    }

    public void setCurrentlyEditing(Player player, Arena arena){
        if(!arenas.contains(arena))
            return;

        if(currentlyEditing.containsKey(player.getUniqueId())){
            currentlyEditing.replace(player.getUniqueId(), arena);
        }

        currentlyEditing.put(player.getUniqueId(), arena);

        player.sendMessage(ChatColor.GREEN + "Now editing arena " + ChatColor.AQUA + arena.getTitle());
    }

    public Arena createArena(Player player, String title){
        Arena newArena = new Arena(title, this.plugin);
        newArena.setMaxPlayers(10);
        newArena.setMinPlayers(2);
        newArena.setBlueTeam(new Team());
        newArena.setRedTeam(new Team());
        arenas.add(newArena);

        Arena returnArena = arenas.get(arenas.indexOf(newArena));
        setCurrentlyEditing(player, returnArena);
        return returnArena;
    }

    public void getArenas(){
        this.arenas = new ArrayList<>();
        ConfigurationSection arena = plugin.getConfig().getConfigurationSection("arenas");
        if(arena != null){
            for(String title : arena.getKeys(false)){
                String path = "arenas." + title;
                Arena a = new Arena(title, this.plugin);
                a.setEndLocation(LocationUtil.getLocationWithDirection(path + ".end-location", plugin));
                a.setLobbyLocation(LocationUtil.getLocationWithDirection(path + ".lobby-location", plugin));
                a.setMaxPlayers(plugin.getConfig().getInt(path + ".max-players"));
                a.setMinPlayers(plugin.getConfig().getInt(path + ".min-players"));
                a.setActivated(true);
                ConfigurationSection blueTeam = plugin.getConfig().getConfigurationSection(path + ".blue-spawns");
                if(blueTeam != null){
                    Team team = new Team();
                    for(String i : blueTeam.getKeys(false)){
                        String p = path + ".blue-spawns." + i;
                        team.addLocation(LocationUtil.getLocationWithDirection(p, plugin));
                    }
                    a.setBlueTeam(team);
                }
                ConfigurationSection redTeam = plugin.getConfig().getConfigurationSection(path + ".red-spawns");
                if(redTeam != null){
                    Team team = new Team();
                    for(String i : redTeam.getKeys(false)){
                        String p = path + ".red-spawns." + i;
                        team.addLocation(LocationUtil.getLocationWithDirection(p, plugin));
                    }
                    a.setRedTeam(team);
                }

                ArrayList<Sign> signs = new ArrayList<>();

                ConfigurationSection signSection = plugin.getConfig().getConfigurationSection(path + ".signs");
                if(signSection != null) {
                    for (String i : signSection.getKeys(false)) {
                        String p = path + ".signs." + i;
                        Location loc = LocationUtil.getLocationWithDirection(p, plugin);
                        World w = loc.getWorld();
                        Block b = w.getBlockAt(loc);
                        if (b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN) {
                            signs.add((Sign) b.getState());
                        }
                    }
                    a.setSigns(signs);
                }
                a.updateSigns();
                arenas.add(a);
            }
        }
    }

    public void saveArenas(){
        for(Arena a : arenas){
            if(!a.isActivated())
                continue;
            String path = "arenas." + a.getTitle();
            plugin.getConfig().set(path + ".max-players", a.getMaxPlayers());
            plugin.getConfig().set(path + ".min-players", a.getMinPlayers());
            LocationUtil.saveLocation(path + ".end-location", a.getEndLocation(), plugin);
            LocationUtil.saveLocation(path + ".lobby-location", a.getLobbyLocation(), plugin);
            int i = 0;
            for(Location loc : a.getBlueTeam().getSpawnLocations()){
                LocationUtil.saveLocation(path + ".blue-spawns." + String.valueOf(i), loc, plugin);
                i++;
            }
            i = 0;
            for(Location loc : a.getRedTeam().getSpawnLocations()){
                LocationUtil.saveLocation(path + ".red-spawns." + String.valueOf(i), loc, plugin);
                i++;
            }
            i = 0;
            for(Sign s : a.getSigns()){
                LocationUtil.saveLocation(path + ".signs." + String.valueOf(i), s.getLocation(), plugin);
                i++;
            }
        }

        plugin.saveConfig();
    }
}

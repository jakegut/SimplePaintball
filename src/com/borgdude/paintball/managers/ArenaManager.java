package com.borgdude.paintball.managers;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.objects.Arena;
import com.borgdude.paintball.objects.ArenaState;
import com.borgdude.paintball.objects.Team;
import com.borgdude.paintball.utils.LocationUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private HashMap<String, Arena> arenas;
    private HashMap<UUID, Arena> currentlyEditing;
    private Main plugin;
    private PaintballManager paintballManager;

    public ArenaManager(HashMap<String, Arena> arenas, Main plugin) {
        this.arenas = arenas;
        this.plugin = plugin;
        this.currentlyEditing = new HashMap<>();
        this.paintballManager = plugin.getPaintballManager();
    }

    public HashMap<String, Arena> getArena() {
        return arenas;
    }

    public ArrayList<Arena> getActivatedArenas() {
        ArrayList<Arena> r = new ArrayList<>();

        for (Arena a : getArena().values())
            if (a.isActivated())
                r.add(a);

        return r;

    }

    public void addPlayerToArena(Player player, Arena a) throws IOException {
        if (getPlayerArena(player) != null) {
            player.sendMessage(plugin.getLanguageManager().getMessage("Arena.Leave-First"));
            return;
        }

        if (a.getArenaState() == ArenaState.IN_GAME || a.getArenaState() == ArenaState.RESTARTING
                || a.getArenaState() == ArenaState.ENDING) {
            player.sendMessage(
                    plugin.getLanguageManager().getMessage("Join.Arena-In-Game".replace("%title%", a.getTitle())));
            return;
        }

        player.teleport(a.getLobbyLocation());
        a.addPlayer(player);
        player.setGameMode(GameMode.ADVENTURE);
//        a.getPlayers().add(player.getUniqueId());
//        Main.inventoryManager.saveInventory(player);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        a.setGunKit(player, paintballManager.getGunByName("Sniper"));
        player.sendMessage(plugin.getLanguageManager().getMessage("Join.Success").replace("%title%", a.getTitle()));
        a.addLobbyItems(player);

        a.checkToStart();

    }

    public void addSpectatorToArena(Player player, Arena a) {
        if (getPlayerArena(player) != null) {
            player.sendMessage(plugin.getLanguageManager().getMessage("Arena.Leave-First"));
            return;
        }

        if (getSpectatorArena(player) != null) {
            player.sendMessage(plugin.getLanguageManager().getMessage("Arena.Leave-First"));
            return;
        }

        player.setGameMode(GameMode.SPECTATOR);
        a.getSpectators().add(player.getUniqueId());
        player.teleport(a.getRandomLocation());
        if(a.getScoreboard() != null)
            player.setScoreboard(a.getScoreboard());
        player.sendMessage(plugin.getLanguageManager().getMessage("Spectate.Success").replace("%title%", a.getTitle()));
    }

    private void addLobbyItems(Player player, Arena a) {

    }

    public void removePlayerFromArena(Player player) {
        Arena a = getPlayerArena(player);

        if (a == null) {
            if (removeSpectatorFromArena(player)) {
            } else {
                player.sendMessage(plugin.getLanguageManager().getMessage("Arena.Not-In"));
            }
            return;
        }

        if (a.getArenaState().equals(ArenaState.IN_GAME)) {
            a.removePlayerInGame(player);
        } else {
            a.removePlayer(player);
            return;
        }

    }

    public boolean removeSpectatorFromArena(Player player) {
        Arena a = getSpectatorArena(player);

        if (a == null)
            return false;

        a.getSpectators().remove(player.getUniqueId());
        player.teleport(a.getEndLocation());
        player.setGameMode(Bukkit.getDefaultGameMode());
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        return true;

    }

    public Arena getPlayerArena(Player player) {
        UUID pUUID = player.getUniqueId();

        for (Arena a : getArena().values()) {
            if (a.getPlayers().contains(pUUID))
                return a;
        }

        return null;
    }

    public Arena getSpectatorArena(Player player) {
        UUID pUUID = player.getUniqueId();

        for (Arena a : getArena().values()) {
            if (a.getSpectators().contains(pUUID))
                return a;
        }

        return null;
    }

    public Arena getArenaByTitle(String title) {
        return arenas.get(title);
    }

    public Arena getCurrentlyEditing(Player player) {
        if (currentlyEditing.containsKey(player.getUniqueId())) {
            return currentlyEditing.get(player.getUniqueId());
        }

        return null;
    }

    public void setCurrentlyEditing(Player player, Arena arena) {
        if (!arenas.containsKey(arena.getTitle()))
            return;

        if (currentlyEditing.containsKey(player.getUniqueId())) {
            currentlyEditing.replace(player.getUniqueId(), arena);
        }

        currentlyEditing.put(player.getUniqueId(), arena);

        player.sendMessage(
                plugin.getLanguageManager().getMessage("Edit.Now-Editing-Arena").replace("%title%", arena.getTitle()));
    }

    public Arena createArena(Player player, String title) {
        Arena newArena = new Arena(title, this.plugin);
        newArena.setMaxPlayers(10);
        newArena.setMinPlayers(2);
        arenas.put(title, newArena);

        Arena returnArena = arenas.get(title);
        setCurrentlyEditing(player, returnArena);
        return returnArena;
    }

    public String removeArena(String title) {
        Arena remove = getArenaByTitle(title);
        if (remove == null)
            return plugin.getLanguageManager().getMessage("Edit.Arena-Not-Found").replace("%title%", title);
        if (!remove.getArenaState().equals(ArenaState.WAITING_FOR_PLAYERS))
            return plugin.getLanguageManager().getMessage("Edit.Remove-In-Progress");
        for (Location loc : remove.getSignLocations()) {
            BlockData d = loc.getBlock().getBlockData();
            if (d instanceof Directional) {
                Directional directional = (Directional) d;
                Block blockBehind = loc.getBlock().getRelative(directional.getFacing().getOppositeFace());
                blockBehind.setType(Material.AIR);
            }
            loc.getBlock().setType(Material.AIR);
        }
        arenas.remove(title);
        return plugin.getLanguageManager().getMessage("Edit.Remove-Success").replace("%title%", title);
    }

    private HashMap<String, Arena> getArenas(ConfigurationSection arenas) {
        HashMap<String, Arena> as = new HashMap<>();
        if (arenas != null) {
            for (String title : arenas.getKeys(false)) {
                ConfigurationSection arena = arenas.getConfigurationSection(title);
                Arena a = new Arena(title, this.plugin);
                a.setEndLocation(LocationUtil.getLocationWithDirection(arena.getConfigurationSection("end-location")));
                a.setLobbyLocation(
                        LocationUtil.getLocationWithDirection(arena.getConfigurationSection("lobby-location")));
                a.setMaxPlayers(arena.getInt("max-players"));
                a.setMinPlayers(arena.getInt("min-players"));
                a.checkMinMax();
                a.setActivated(true);
                for(Team t : a.getTeams().values()) {
                    ConfigurationSection teamSpawns = arena.getConfigurationSection(t.getName().toLowerCase() + "-spawns");
                    if (teamSpawns != null) {
                        for (String i : teamSpawns.getKeys(false)) {
                            ConfigurationSection location = teamSpawns.getConfigurationSection(i);
                            t.addLocation(LocationUtil.getLocationWithDirection(location));
                        }
                    }
                }

                ArrayList<Location> signs = new ArrayList<>();
                ConfigurationSection signSection = arena.getConfigurationSection("signs");
                if (signSection != null) {
                    for (String i : signSection.getKeys(false)) {
                        ConfigurationSection section = signSection.getConfigurationSection(i);
                        Location loc = LocationUtil.getLocationWithDirection(section);
                        signs.add(loc);
//                        World w = loc.getWorld();
//                        if(w == null) continue;
//                        Block b = w.getBlockAt(loc);
//                        if(b == null) continue;
//                        if (b.getState() instanceof Sign)
//                            signs.add((Sign) b.getState());
                    }
                }
                a.setSignLocations(signs);
                a.updateSigns();
                as.put(a.getTitle(), a);
            }
        }
        return as;
    }

    public void getArenas() {
        plugin.getPluginLogger().log("Loading arenas...");
        this.arenas = new HashMap<>();
        ConfigurationSection arena = plugin.getConfig().getConfigurationSection("arenas");
        if (arena != null) {
            arenas.putAll(getArenas(arena));
            plugin.getConfig().set("arenas", null);
            plugin.saveConfig();
        }

        arena = plugin.getArenaConfig().getConfigurationSection("arenas");
        if(arena != null)
            arenas.putAll(getArenas(plugin.getArenaConfig().getConfigurationSection("arenas")));

        for(Arena a : arenas.values()){
            plugin.getPluginLogger().log("\tTitle: " + a.getTitle());
            plugin.getPluginLogger().log("\tSpawns:");
            for(Team t : a.getTeams().values()){
                plugin.getPluginLogger().log("\t\tTeam: " + t.getName());
                for(Location loc : t.getSpawnLocations()) {
                    plugin.getPluginLogger().log("\t\t\tLocation: " + loc);
                }
            }
            plugin.getPluginLogger().log("\tSigns:");
            for(Location loc : a.getSignLocations()) {
                plugin.getPluginLogger().log("\t\tLocation: " + loc);
            }
        }

        plugin.getPluginLogger().log("Loading arenas...DONE");
    }

    public void saveArenas() {
        plugin.getArenaConfig().set("arenas", null);
        for (Arena a : arenas.values()) {
            if (!a.isActivated())
                continue;
            String p = "arenas." + a.getTitle();
            plugin.getArenaConfig().set(p + ".max-players", a.getMaxPlayers());
            plugin.getArenaConfig().set(p + ".min-players", a.getMinPlayers());
            LocationUtil.saveLocation(p + ".end-location", a.getEndLocation(), plugin.getArenaConfig());
            LocationUtil.saveLocation(p + ".lobby-location", a.getLobbyLocation(), plugin.getArenaConfig());
            int i = 0;
            for(Team t : a.getTeams().values()) {
                i = 0;
                plugin.getArenaConfig().set(p + "." + t.getName().toLowerCase() + "-spawns", null);
                if(t.getSpawnLocations().size() > 0) {
                    for(Location loc : t.getSpawnLocations()) {
                        LocationUtil.saveLocation(p + "." + t.getName().toLowerCase() + "-spawns." + String.valueOf(i), loc, plugin.getArenaConfig());
                        i++;
                    }
                }
            }
//            for (Location loc : a.getBlueTeam().getSpawnLocations()) {
//                LocationUtil.saveLocation(p + ".blue-spawns." + String.valueOf(i), loc, plugin.getArenaConfig());
//                i++;
//            }
//            plugin.getArenaConfig().set(p + ".red-spawns", null);
//            i = 0;
//            for (Location loc : a.getRedTeam().getSpawnLocations()) {
//                LocationUtil.saveLocation(p + ".red-spawns." + String.valueOf(i), loc, plugin.getArenaConfig());
//                i++;
//            }
            plugin.getArenaConfig().set(p + ".signs", null);
            i = 0;
            for (Location loc : a.getSignLocations()) {
                LocationUtil.saveLocation(p + ".signs." + String.valueOf(i), loc, plugin.getArenaConfig());
                i++;
            }
        }

        try {
            plugin.saveArenaConfig();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

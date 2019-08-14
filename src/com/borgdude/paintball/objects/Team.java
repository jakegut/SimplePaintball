package com.borgdude.paintball.objects;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.borgdude.paintball.utils.ColorUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class Team {

    private HashSet<UUID> members;
    private Color color;
    private ChatColor chatColor;
    private String name;

    public Team(ChatColor color, String name) {
        this(new ArrayList<>(), color, name);
    }

    public Team(ArrayList<Location> locations, ChatColor color, String name) {
        spawnLocations = locations;
        members = new HashSet<>();
        this.color = ColorUtil.translateChatColorToColor(color);
        this.chatColor = color;
        this.name = StringUtils.capitalize(name);
    }

    public HashSet<UUID> getMembers() {
        return members;
    }

    public void setMembers(HashSet<UUID> members) {
        this.members = members;
    }

    public ItemStack[] generateArmor() {
        ItemStack[] r = new ItemStack[4];

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta hm = (LeatherArmorMeta) helmet.getItemMeta();
        hm.setDisplayName("Paintball Helmet");
        hm.setUnbreakable(true);
        hm.setColor(color);
        helmet.setItemMeta(hm);

        r[3] = helmet;

        ItemStack chestPlate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta cp = (LeatherArmorMeta) chestPlate.getItemMeta();
        cp.setDisplayName("Paintball Chestplate");
        cp.setUnbreakable(true);
        cp.setColor(color);
        chestPlate.setItemMeta(cp);

        r[2] = chestPlate;

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta lm = (LeatherArmorMeta) leggings.getItemMeta();
        lm.setDisplayName("Paintball Leggings");
        lm.setUnbreakable(true);
        lm.setColor(color);
        leggings.setItemMeta(lm);

        r[1] = leggings;

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bm = (LeatherArmorMeta) boots.getItemMeta();
        bm.setDisplayName("Paintball Boots");
        bm.setUnbreakable(true);
        bm.setColor(color);
        boots.setItemMeta(bm);

        r[0] = boots;

        return r;
    }

    public void giveArmor() {
        ItemStack[] armorSet = generateArmor();
        for (UUID id : getMembers()) {
            Player p = Bukkit.getPlayer(id);
            p.getInventory().setArmorContents(armorSet);
        }
    }

    public boolean containsPlayer(Player p) {
        return members.contains(p.getUniqueId());
    }

    public ArrayList<Location> getSpawnLocations() {
        return spawnLocations;
    }

    public void setSpawnLocations(ArrayList<Location> spawnLocations) {
        this.spawnLocations = spawnLocations;
    }

    private ArrayList<Location> spawnLocations;

    public void addMember(Player player) {
        UUID pUUID = player.getUniqueId();
        if (!members.contains(pUUID)) {
            members.add(pUUID);
        }
    }

    public void addLocation(Location loc) {
        if (!spawnLocations.contains(loc))
            spawnLocations.add(loc);
    }

    public Location getRandomLocation() {
        Random r = new Random();
        return spawnLocations.get(r.nextInt(spawnLocations.size()));
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getChatName() {
        return this.chatColor + this.name;
    }
    
    public ChatColor getChatColor() {
        return this.chatColor;
    }
    
    public String getScoreboardID() {
        return this.getName().toLowerCase() + "PBKC";
    }
}

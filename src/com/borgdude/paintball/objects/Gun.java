package com.borgdude.paintball.objects;

import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;

public abstract class Gun {

    public abstract ItemStack getLobbyItem();

    public abstract ItemStack getInGameItem();

    public abstract int getCooldown();

    public abstract void fire(final Player player);

    public abstract void onHit(Player player, Snowball ball);

    public abstract String getName();
    
    public String toString() {
        if(getName() == null) return "";
        else return getName();
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
}

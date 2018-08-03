package com.borgdude.paintball.objects;

import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;

public interface Gun {

    ItemStack getLobbyItem();

    ItemStack getInGameItem();

    int getCooldown();

    void fire(final Player player);

    void onHit(Player player, Snowball ball);

    String getName();
}

package com.borgdude.paintball.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public interface ClickAction {

    void execute(Player player, ClickType clickType);

 }

package com.borgdude.paintball.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class IntegerIcon extends Icon{

    public IntegerIcon(ItemStack itemStack) {
        super(itemStack);
        this.addClickAction(new ClickAction() {

            @Override
            public void execute(Player player, ClickType clickType) {
                // TODO Auto-generated method stub
                
            }
            
        });
    }

}

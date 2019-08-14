package com.borgdude.paintball.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public class ColorUtil {
    public static Color translateChatColorToColor(ChatColor chatColor) {
        switch (chatColor) {
        case AQUA:
            return Color.AQUA;
        case BLACK:
            return Color.BLACK;
        case BLUE:
            return Color.BLUE;
        case GRAY:
            return Color.GRAY;
        case GREEN:
            return Color.GREEN;
        case LIGHT_PURPLE:
            return Color.PURPLE;
        case RED:
            return Color.RED;
        case WHITE:
            return Color.WHITE;
        case YELLOW:
            return Color.YELLOW;
        default:
            break;
        }
        return null;
    }

    public static String ChatColorToString(ChatColor cc) {
        switch(cc) {
        case AQUA:
            return "Aqua";
        case BLACK:
            return "Black";
        case BLUE:
            return "Blue";
        case GRAY:
            return "Gray";
        case GREEN:
            return "Green";
        case LIGHT_PURPLE:
            return "Purple";
        case RED:
            return "Red";
        case WHITE:
            return "White";
        case YELLOW:
            return "Yellow";
        default:
            break;
        }

        return null;
    }
}

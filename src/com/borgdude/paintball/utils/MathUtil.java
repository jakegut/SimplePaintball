package com.borgdude.paintball.utils;

import org.bukkit.util.Vector;

public class MathUtil {

    public static Vector getRandomVector(float accuracy) {
        return new Vector(getRandomComp(accuracy), getRandomComp(accuracy), getRandomComp(accuracy));
    }

    public static float getRandomComp(float accuracy) {
        return (float) (Math.random() * (accuracy * 2) - accuracy);
    }
}

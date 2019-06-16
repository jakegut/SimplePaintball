package com.borgdude.paintball.utils;

import org.bukkit.util.Vector;

public class MathUtil {

    public static Vector getRandomVector(float accuracy) {
        return new Vector(getRandomComp(accuracy), getRandomComp(accuracy), getRandomComp(accuracy));
    }

    public static float getRandomComp(float accuracy) {
        return (float) (Math.random() * (accuracy * 2) - accuracy);
    }
    
    public static double tanh(double x) {
    	if(x < 0 || x > 5) return 0;
    	else return -x + 5;
    }
    
    public static int getMaxHeight(Vector v) {
    	return (int) (- (v.getY() * v.getY()) / (2 * 0.08));
    }
}

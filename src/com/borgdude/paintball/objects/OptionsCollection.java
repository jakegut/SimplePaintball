package com.borgdude.paintball.objects;

import java.util.HashMap;

public class OptionsCollection<T> {
    
    private HashMap<String, T> options;
    
    public OptionsCollection(){
        options = new HashMap<>();
    }
    
    public T getOption(String id) {
        return options.get(id);
    }
    
    public void setOption(String id, T option) {
        options.put(id, option);
    }
    
    
}

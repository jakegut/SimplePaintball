package com.borgdude.paintball.objects;

public class ArenaOptions {
    
    OptionsCollection<Integer> integerOptions;
    OptionsCollection<Boolean> booleanOptions;
    
    public ArenaOptions() {
        registerOptions();
    }

    private void registerOptions() {
        integerOptions = new OptionsCollection<>();
        integerOptions.setOption("max-players", 10);
        integerOptions.setOption("min-players", 2);
        
        booleanOptions = new OptionsCollection<>();
        booleanOptions.setOption("player-death", false);
    }
}

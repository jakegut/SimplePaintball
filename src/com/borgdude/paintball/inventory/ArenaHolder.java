package com.borgdude.paintball.inventory;

import com.borgdude.paintball.objects.Arena;

public class ArenaHolder extends CustomHolder{
    
    private final Arena arena;

    public ArenaHolder(int size, String title, Arena arena) {
        super(size, "Edit " + arena.getTitle());
        this.arena = arena;
    }
    
    public Arena getArena() {
        return this.arena;
    }

}

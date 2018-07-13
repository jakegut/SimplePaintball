package com.borgdude.paintball.objects;

public enum ArenaState {
    WAITING_FOR_PLAYERS("Waiting"), STARTING("Starting"), IN_GAME("Playing"), ENDING("Ending"), RESTARTING("Restarting");

    String formattedName;

    ArenaState(String formattedName) {
        this.formattedName = formattedName;
    }

    public String getFormattedName() {
        return formattedName;
    }
}

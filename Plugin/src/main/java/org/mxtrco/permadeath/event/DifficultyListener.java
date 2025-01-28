package org.mxtrco.permadeath.event;

import org.bukkit.event.Listener;

public class DifficultyListener implements Listener {

    private int difficultyLevel;

    // Constructor para pasar el nivel de dificultad
    public DifficultyListener(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}

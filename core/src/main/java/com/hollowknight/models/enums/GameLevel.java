package com.hollowknight.models.enums;

import com.badlogic.gdx.math.Vector2;

public enum GameLevel {
    FORGOTTEN_CROSSROADS("map/forgotten_crossroads.tmx");

    public String tmxPath;

    GameLevel(String tmxPaString) {
        this.tmxPath = tmxPaString;
    }

    public Vector2 getSpawnPoint() {
        return new Vector2();
    }
}

package com.hollowknight.models.gamedata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.hollowknight.models.GameWorld;
import com.hollowknight.models.settings.Settings;

public class Saver {
    public static void saveSettings(Settings settings) {
        Json json = new Json();
        String saveData = json.toJson(settings);
        FileHandle file = Gdx.files.local("gamedata/settings.json");
        file.writeString(saveData, false);
    }

    public static void saveGame(GameWorld game) {
        Json json = new Json();
        String saveData = json.toJson(game);
        FileHandle file = Gdx.files.local("gamedata/saves/" + game.getWorldName() + ".json");
        file.writeString(saveData, false);
    }
}

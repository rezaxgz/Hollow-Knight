package com.hollowknight.models.gamedata;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.hollowknight.models.Game;
import com.hollowknight.models.settings.Settings;

public class Loader {
    public static Settings loadSettings() {
        Json json = new Json();
        FileHandle file = Gdx.files.local("gamedata/settings.json");
        Settings settings;

        if (file.exists()) {
            try {
                settings = json.fromJson(Settings.class, file.readString());
            } catch (Exception e) {
                settings = new Settings();
                Saver.saveSettings(settings);
            }
        } else {
            settings = new Settings();
            Saver.saveSettings(settings);
        }
        return settings;
    }

    public static List<Game> loadSaves() {
        List<Game> saves = new ArrayList<>();

        FileHandle saveDir = Gdx.files.local("gamedata/saves");

        if (!saveDir.exists()) {
            saveDir.mkdirs();
            return saves;
        }

        Json json = new Json();

        for (FileHandle file : saveDir.list(".json")) {
            try {
                Game save = json.fromJson(Game.class, file);
                if (save != null) {
                    saves.add(save);
                }
            } catch (Exception e) {
            }
        }

        return saves;
    }
}

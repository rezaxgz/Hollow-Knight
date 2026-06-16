package com.hollowknight.models.gamedata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.hollowknight.models.settings.Settings;

public class Loader {
    public static Settings loadSettings() {
        Json json = new Json();
        FileHandle file = Gdx.files.local("gamedata/settings.json");
        Settings settings;

        if (file.exists()) {
            settings = json.fromJson(Settings.class, file.readString());
        } else {
            settings = new Settings();
            Saver.saveSettings(settings);
        }

        return settings;
    }
}

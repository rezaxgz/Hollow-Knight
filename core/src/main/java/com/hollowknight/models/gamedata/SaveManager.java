package com.hollowknight.models.gamedata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.models.world.GameWorld;

public class SaveManager {

    private static final int MAX_SLOTS = 4;
    private static final String SAVE_PREFIX = "hk_save_slot_";
    private static final String SAVE_EXTENSION = ".json";

    private static final Json json = new Json();

    static {
        // Formats JSON to be readable and disables prototypes to prevent bloat
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
    }

    /**
     * Pulls active stats from the world, populates the GameSave, and writes it to
     * disk.
     */
    public static void saveGame(GameWorld world) {
        int slot = world.saveLoadedFrom.slot;
        GameSave currentSave = world.saveLoadedFrom;
        if (slot < 1 || slot > MAX_SLOTS) {
            Gdx.app.error("SaveManager", "Invalid save slot: " + slot + ".");
            return;
        }

        // Sync dynamic world data into the save object before serializing
        currentSave.totalPassedTime = world.getTotalPassedTime();
        currentSave.numberOfEnemiesKilled = world.getNumberOfEnemiesKilled();
        currentSave.numberOfDeaths = world.getNumberOfDeaths();
        currentSave.player = world.player;

        FileHandle saveFile = Gdx.files.local(SAVE_PREFIX + slot + SAVE_EXTENSION);

        try {
            String jsonStr = json.prettyPrint(currentSave);
            saveFile.writeString(jsonStr, false);
            Gdx.app.log("SaveManager", "Game successfully saved to slot " + slot);
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to save game to slot " + slot, e);
        }
    }

    /**
     * Reads a slot from disk and reconstructs a playable GameSave instance.
     */
    public static GameSave loadGame(int slot) {
        if (slot < 1 || slot > MAX_SLOTS) {
            Gdx.app.error("SaveManager", "Invalid save slot: " + slot + ".");
            return null;
        }

        FileHandle saveFile = Gdx.files.local(SAVE_PREFIX + slot + SAVE_EXTENSION);

        if (!saveFile.exists()) {
            return null; // Useful for UI logic
        }

        try {
            return json.fromJson(GameSave.class, saveFile);
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to load game from slot " + slot, e);
            return null;
        }
    }

    /**
     * Quick check if a slot is empty (use this to enable/disable "Load Game"
     * buttons in UI)
     */
    public static boolean isSlotEmpty(int slot) {
        return !Gdx.files.local(SAVE_PREFIX + slot + SAVE_EXTENSION).exists();
    }

    /**
     * Wipes a saved file
     */
    public static void deleteSave(int slot) {
        FileHandle saveFile = Gdx.files.local(SAVE_PREFIX + slot + SAVE_EXTENSION);
        if (saveFile.exists()) {
            saveFile.delete();
            Gdx.app.log("SaveManager", "Deleted save file in slot " + slot);
        }
    }

    public static Settings loadSettings() {
        return new Settings();
    }

    public static void saveSettings(Settings s) {
    }

    public static int getFirstEmptySlot() {
        for (int i = 1; i < 5; i++) {
            if (isSlotEmpty(i))
                return i;
        }
        return 4;
    }
}
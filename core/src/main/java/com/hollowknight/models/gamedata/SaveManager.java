package com.hollowknight.models.gamedata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.models.world.GameWorld;

public class SaveManager {

    private static final int MAX_SLOTS = 4;
    private static final String SAVE_DIRECTORY = "gamedata/";
    private static final String SAVE_PREFIX = "hk_save_slot_";
    private static final String SAVE_EXTENSION = ".json";

    private static final Json json = new Json();

    static {
        // Formats JSON to be readable and disables prototypes to prevent bloat
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
    }

    public static void saveGame(GameWorld world) {
        int slot = world.saveLoadedFrom.slot;
        GameSave currentSave = world.saveLoadedFrom;
        if (slot < 0 || slot >= MAX_SLOTS) {
            Gdx.app.error("SaveManager", "Invalid save slot: " + slot + ".");
            return;
        }

        currentSave.totalPassedTime = world.getTotalPassedTime();
        currentSave.numberOfEnemiesKilled = world.getNumberOfEnemiesKilled();
        currentSave.numberOfDeaths = world.getNumberOfDeaths();
        currentSave.player = world.player;

        // Save Boss specific state
        currentSave.bossFightActivated = world.bossFightActivated;
        currentSave.bossFightCompleted = world.bossFightCompleted;

        if (world.bossFightActivated && !world.bossFightCompleted) {
            for (com.hollowknight.models.enemies.Enemy enemy : world.enemies) {
                if (enemy instanceof com.hollowknight.models.enemies.FalseKnight) {
                    com.hollowknight.models.enemies.FalseKnight fk = (com.hollowknight.models.enemies.FalseKnight) enemy;
                    currentSave.bossHp = fk.getHp();
                    currentSave.bossState = fk.currentState.name();
                    currentSave.bossX = fk.position.x;
                    currentSave.bossY = fk.position.y;
                    break;
                }
            }
        }

        FileHandle saveFile = Gdx.files.local(SAVE_DIRECTORY + SAVE_PREFIX + slot + SAVE_EXTENSION);

        try {
            String jsonStr = json.prettyPrint(currentSave);
            // LibGDX automatically creates parent directories if they don't exist when
            // writing
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
        if (slot < 0 || slot >= MAX_SLOTS) {
            Gdx.app.error("SaveManager", "Invalid save slot: " + slot + ".");
            return null;
        }

        FileHandle saveFile = Gdx.files.local(SAVE_DIRECTORY + SAVE_PREFIX + slot + SAVE_EXTENSION);

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
        return !Gdx.files.local(SAVE_DIRECTORY + SAVE_PREFIX + slot + SAVE_EXTENSION).exists();
    }

    /**
     * Wipes a saved file
     */
    public static void deleteSave(int slot) {
        FileHandle saveFile = Gdx.files.local(SAVE_DIRECTORY + SAVE_PREFIX + slot + SAVE_EXTENSION);
        if (saveFile.exists()) {
            saveFile.delete();
            Gdx.app.log("SaveManager", "Deleted save file in slot " + slot);
        }
    }

    public static Settings loadSettings() {
        // 1. Load Global Achievements
        FileHandle achFile = Gdx.files.local(SAVE_DIRECTORY + "achievements.json");
        if (achFile.exists()) {
            try {
                String[] unlocked = json.fromJson(String[].class, achFile);
                if (unlocked != null) {
                    com.hollowknight.models.achievements.AchievementManager.getInstance()
                            .loadUnlockedAchievements(java.util.Arrays.asList(unlocked));
                }
            } catch (Exception e) {
                Gdx.app.error("SaveManager", "Failed to load achievements", e);
            }
        }

        // 2. Load Global Settings
        FileHandle file = Gdx.files.local(SAVE_DIRECTORY + "settings.json");
        if (file.exists()) {
            try {
                return json.fromJson(Settings.class, file);
            } catch (Exception e) {
                Gdx.app.error("SaveManager", "Failed to load settings", e);
            }
        }
        return new Settings();
    }

    public static void saveSettings(Settings s) {
        // 1. Save Global Settings
        FileHandle file = Gdx.files.local(SAVE_DIRECTORY + "settings.json");
        file.writeString(json.prettyPrint(s), false);

        // 2. Save Global Achievements
        java.util.List<String> unlockedList = com.hollowknight.models.achievements.AchievementManager.getInstance()
                .getUnlockedIds();
        String[] unlockedArray = unlockedList.toArray(new String[0]);
        FileHandle achFile = Gdx.files.local(SAVE_DIRECTORY + "achievements.json");
        achFile.writeString(json.prettyPrint(unlockedArray), false);
    }

    public static int getFirstEmptySlot() {
        // Fixed: Loop through valid index range (0 to MAX_SLOTS - 1)
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (isSlotEmpty(i)) {
                return i;
            }
        }
        return -1; // Return -1 or another fallback if all slots are full
    }
}
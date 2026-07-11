package com.hollowknight.controller;

import com.hollowknight.models.gamedata.SaveManager;
import com.hollowknight.models.settings.Settings;

public class GeneralController {
    public static void exitApp() {
        // save progress
        SaveManager.saveSettings(Settings.getInstance());
        GameController gc = GameController.getInstance();
        if (gc != null && gc.world != null) {
            SaveManager.saveGame(gc.world);
        }

    }

    public static void loadData() {
        SaveManager.loadSettings();
    }
}

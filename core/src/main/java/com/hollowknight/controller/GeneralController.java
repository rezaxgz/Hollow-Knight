package com.hollowknight.controller;

import com.hollowknight.models.gamedata.Saver;
import com.hollowknight.models.settings.Settings;

public class GeneralController {
    public static void exitApp() {
        // save progress
        Saver.saveSettings(Settings.getInstance());
    }
}

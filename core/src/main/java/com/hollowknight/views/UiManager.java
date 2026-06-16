package com.hollowknight.views;

import com.badlogic.gdx.Screen;
import com.hollowknight.views.screens.AbstractScreen;

public class UiManager {
    private static Main main;

    public static void init(Main main) {
        UiManager.main = main;
    }

    public static void setScreen(Screen screen) {
        main.setScreen(screen);
    }

    public static AbstractScreen getScreen() {
        if (main.getScreen() instanceof AbstractScreen abstractScreen) {
            return abstractScreen;
        } else {
            return null;
        }
    }
}

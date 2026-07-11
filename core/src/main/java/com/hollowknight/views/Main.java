package com.hollowknight.views;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hollowknight.controller.GeneralController;
import com.hollowknight.views.screens.MainMenuScreen;

public class Main extends Game {
    @Override
    public void create() {
        GameAssetManager.init();
        UiManager.init(this);
        GeneralController.loadData();

        MainMenuScreen mainMenuScreen = new MainMenuScreen();
        setScreen(mainMenuScreen);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 0);
        super.render();
    }
}

package com.hollowknight.views;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hollowknight.models.gamedata.Saver;
import com.hollowknight.views.screens.MainMenuScreen;

public class Main extends Game {
    @Override
    public void create() {
        Saver.saveGame(new com.hollowknight.models.Game());
        GameAssetManager.init();
        UiManager.init(this);

        MainMenuScreen mainMenuScreen = new MainMenuScreen();
        setScreen(mainMenuScreen);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 0);
        super.render();
    }
}

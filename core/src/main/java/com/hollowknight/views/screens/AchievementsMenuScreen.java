package com.hollowknight.views.screens;

import com.hollowknight.controller.AudioController;
import com.hollowknight.views.GameAssetManager;

public class AchievementsMenuScreen extends AbstractScreen {
    @Override
    public void show() {
        super.show();
        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }
}

package com.hollowknight.views.actors.modals;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hollowknight.controller.GameController;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.models.gamedata.SaveManager;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.screens.MainMenuScreen;

public class BossDefeatModal extends Modal {

    public BossDefeatModal(final GameWorld world) {
        super();

        defaults().space(15);

        // Title
        Label titleLabel = new Label("VICTORY!", skin);
        titleLabel.setFontScale(1.5f);
        add(titleLabel).padBottom(20).colspan(2).row();

        add(new Label("Number of Deaths: " + world.getNumberOfDeaths(), skin)).colspan(2).row();
        add(new Label("Enemies Killed: " + world.getNumberOfEnemiesKilled(), skin)).colspan(2).row();
        add(new Label("Total Time Played: " + world.getTotalPassedTime(), skin)).padBottom(30).colspan(2).row();

        TextButton saveRestartBtn = new TextButton("Save and Restart", skin);
        TextButton mainMenuBtn = new TextButton("Return to Main Menu", skin);

        add(saveRestartBtn).width(250).colspan(2).row();
        add(mainMenuBtn).width(250).colspan(2).row();

        saveRestartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Completely save progress and settings
                SaveManager.saveGame(world);
                SaveManager.saveSettings(Settings.getInstance());

                System.out.println("Game Saved! Restarting...");
                GameController.getInstance().isPaused = false;
                hide();
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        mainMenuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameController.getInstance().isPaused = false;
                hide();
                UiManager.setScreen(new MainMenuScreen());
            }
        });
    }
}
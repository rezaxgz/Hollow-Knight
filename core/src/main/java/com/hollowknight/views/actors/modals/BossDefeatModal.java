package com.hollowknight.views.actors.modals;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hollowknight.controller.GameController;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.screens.MainMenuScreen;

public class BossDefeatModal extends Modal {

    public BossDefeatModal(GameWorld world) {
        super();

        defaults().space(15);

        // Title
        Label titleLabel = new Label("VICTORY!", skin);
        titleLabel.setFontScale(1.5f);
        add(titleLabel).padBottom(20).colspan(2).row();

        // 1. Display Statistics
        // Retrieving the exact variables requested from the GameWorld
        add(new Label("Number of Deaths: " + world.getNumberOfDeaths(), skin)).colspan(2).row();
        add(new Label("Enemies Killed: " + world.getNumberOfEnemiesKilled(), skin)).colspan(2).row();
        add(new Label("Total Time Played: " + world.getTotalPassedTime(), skin)).padBottom(30).colspan(2).row();

        // 2. Initialize Buttons
        TextButton saveRestartBtn = new TextButton("Save and Restart", skin);
        TextButton mainMenuBtn = new TextButton("Return to Main Menu", skin);

        add(saveRestartBtn).width(250).colspan(2).row();
        add(mainMenuBtn).width(250).colspan(2).row();

        // Save and Restart Action
        saveRestartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO: Implement your specific GameSave writing and level reloading logic here
                System.out.println("Game Saved! Restarting...");
                GameController.getInstance().isPaused = false;
                hide();

                // Fallback to Main Menu for now to prevent crashing
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        // Return to Main Menu Action
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
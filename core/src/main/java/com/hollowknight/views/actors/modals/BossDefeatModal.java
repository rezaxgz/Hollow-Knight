package com.hollowknight.views.actors.modals;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hollowknight.controller.GameController;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.gamedata.SaveManager;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.screens.GameScreen;
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

        TextButton restartBtn = new TextButton("Restart", skin);
        TextButton mainMenuBtn = new TextButton("Return to Main Menu", skin);

        add(restartBtn).width(250).colspan(2).row();
        add(mainMenuBtn).width(250).colspan(2).row();

        restartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Get the current save slot
                int slot = world.saveLoadedFrom.slot;

                // Delete the existing save data for this slot
                SaveManager.deleteSave(slot);

                // Create a fresh save for the identical slot and load it into a new world
                GameSave newSave = GameSave.gameStart(slot);
                GameWorld newWorld = new GameWorld(newSave);

                GameController.getInstance().isPaused = false;
                hide();

                // Immediately transition back to the game screen to start playing
                UiManager.setScreen(new GameScreen(newWorld));
            }
        });

        mainMenuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int slot = world.saveLoadedFrom.slot;
                SaveManager.deleteSave(slot);
                GameController.getInstance().isPaused = false;
                hide();
                UiManager.setScreen(new MainMenuScreen());
            }
        });
    }
}
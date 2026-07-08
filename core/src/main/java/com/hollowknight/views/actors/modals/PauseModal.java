package com.hollowknight.views.actors.modals;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hollowknight.controller.GameController;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.screens.MainMenuScreen;

public class PauseModal extends Modal {
    private InputListener stageListener;

    public PauseModal() {
        super();

        // 1. Initialize Main Buttons
        TextButton resumeBtn = new TextButton("Resume", skin);
        TextButton settingsBtn = new TextButton("Settings", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        defaults().space(10);

        // Title
        add(new Label("PAUSED", skin)).padBottom(20).colspan(2).row();

        // Main Menu Options
        add(resumeBtn).width(150).colspan(2).row();
        add(settingsBtn).width(150).colspan(2).row();
        add(exitButton).width(150).colspan(2).row();

        // 2. Generate Game Cheats Dynamically
        add(new Label("Cheats(Press left Ctrl + Key)", skin)).padTop(20).padBottom(10).colspan(2).row();
        Table cheatsTable = new Table();
        cheatsTable.defaults().space(5);

        int columnCounter = 0;
        for (final GameCheat cheat : GameCheat.values()) {
            TextButton cheatBtn = new TextButton(
                    cheat.name().replace("_", " ") + "(" + Input.Keys.toString(cheat.key) + ")", skin);

            // Use ChangeListener instead of ClickListener
            cheatBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    GameController.getInstance().world.applyCheat(cheat);
                }
            });

            cheatsTable.add(cheatBtn).width(140);
            columnCounter++;
            if (columnCounter % 2 == 0)
                cheatsTable.row();
        }
        add(cheatsTable).colspan(2).row();

        // 3. Attach standard ChangeListeners to the main buttons
        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameController.getInstance().isPaused = false;
                hide(); // Hides/removes the modal
            }
        });

        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO: Show settings modal
                System.out.println("Settings Clicked");
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameController.getInstance().isPaused = false;
                UiManager.setScreen(new MainMenuScreen());
            }
        });
    }

    @Override
    public void show() {
        super.show(); // Adds to the stage stack[cite: 2]

        // Listen to the stage for an ESCAPE key press while this modal is visible
        stageListener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    GameController.getInstance().isPaused = false; // Unpause game[cite: 3]
                    hide(); // Close modal[cite: 3]
                    return true;
                }
                return false;
            }
        };

        if (this.getStage() != null) {
            this.getStage().addListener(stageListener);
        }
    }

    @Override
    public void hide() {
        // Clean up the listener so it doesn't linger in memory
        if (this.getStage() != null && stageListener != null) {
            this.getStage().removeListener(stageListener);
        }
        super.hide(); // Removes modal from the stage stack[cite: 2]
    }
}
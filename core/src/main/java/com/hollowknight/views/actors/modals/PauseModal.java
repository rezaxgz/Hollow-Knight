package com.hollowknight.views.actors.modals;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.hollowknight.controller.GameController;
import com.hollowknight.controller.GeneralController;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.screens.AbstractScreen;
import com.hollowknight.views.screens.MainMenuScreen;
import com.hollowknight.views.screens.SettingsMenuScreen;

public class PauseModal extends Modal {
    private InputListener stageListener;

    public PauseModal() {
        super();

        TextButton resumeBtn = new TextButton("Resume", skin);
        TextButton settingsBtn = new TextButton("Settings", skin);
        TextButton exitButton = new TextButton("Save & Exit", skin); // Updated label for clarity

        defaults().space(10);
        add(new Label("PAUSED", skin)).padBottom(20).colspan(2).row();
        add(resumeBtn).width(150).colspan(2).row();
        add(settingsBtn).width(150).colspan(2).row();
        add(exitButton).width(150).colspan(2).row();

        add(new Label("Cheats(Press left Ctrl + Key)", skin)).padTop(20).padBottom(10).colspan(2).row();
        Table cheatsTable = new Table();
        cheatsTable.defaults().space(5);

        int columnCounter = 0;
        for (final GameCheat cheat : GameCheat.values()) {
            TextButton cheatBtn = new TextButton(
                    cheat.name().replace("_", " ") + "(" + Input.Keys.toString(cheat.key) + ")", skin);

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

        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameController.getInstance().isPaused = false;
                hide();
            }
        });

        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // 1. Fetch the currently active screen (GameScreen) directly from LibGDX
                Screen currentScreen = ((Game) Gdx.app.getApplicationListener()).getScreen();

                // 2. Pass it into the SettingsMenuScreen
                if (currentScreen instanceof AbstractScreen) {
                    UiManager.setScreen(new SettingsMenuScreen((AbstractScreen) currentScreen));
                } else {
                    // Fallback just in case
                    UiManager.setScreen(new SettingsMenuScreen());
                }
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Calls GeneralController to save the current world and global settings
                GeneralController.exitApp();

                GameController.getInstance().isPaused = false;
                hide();
                UiManager.setScreen(new MainMenuScreen());
            }
        });
    }

    @Override
    public void show() {
        super.show();

        stageListener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    GameController.getInstance().isPaused = false;
                    hide();
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
        if (this.getStage() != null && stageListener != null) {
            this.getStage().removeListener(stageListener);
        }
        super.hide();
    }
}
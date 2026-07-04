package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.hollowknight.models.Constants;
import com.hollowknight.models.settings.Controls;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.actors.modals.PauseModal;
import com.hollowknight.views.screens.MainMenuScreen;

public class GameController {
    private static GameController instance;
    GameWorld world;
    PlayerController playerController;

    public static GameController init(GameWorld world) {
        instance = new GameController(world);
        return instance;
    }

    public static GameController getInstance() {
        return instance;
    }

    private GameController(GameWorld world) {
        this.world = world;
        this.playerController = new PlayerController(world.player);
    }

    public void update(float delta) {
        world.update(delta);
    }

    public boolean handleKeyDown(int keycode) {
        Controls controls = Settings.getInstance().getControls();
        if (keycode == Input.Keys.ESCAPE) {
            PauseModal pauseModal = new PauseModal() {
                @Override
                public void onResume() {
                    this.hide();
                }

                @Override
                public void onExit() {
                    UiManager.setScreen(new MainMenuScreen());
                }
            };
            pauseModal.show();
        }

        // Only check for and apply cheats if Ctrl is held
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            GameCheat cheat = controls.getCheat(keycode);

            if (cheat != null) {
                world.applyCheat(cheat);
                return false;
            }
        }

        GameActionType action = controls.getAction(keycode);
        if (action != null) {
            world.player.doAction(action);
        }

        if (world.zote != null) {
            // Start Interaction
            if (keycode == controls.interact) {
                world.zote.interact();
            }
            // Advance Dialogue
            else if (keycode == Input.Keys.ENTER) {
                world.zote.interact();
            }
        }

        return false;
    }

    public boolean handleKeyUp(int keycode) {
        Controls controls = Settings.getInstance().getControls();
        if (keycode == controls.right) {
            world.player.stopMoving(Constants.RIGHT_DIRECTION);
        } else if (keycode == controls.left) {
            world.player.stopMoving(Constants.LEFT_DIRECTION);
        } else if (keycode == controls.focus) {
            world.player.stopFocus();
        } else if (keycode == controls.up) {
            world.player.stopVerticalMovement(Constants.UP_DIRECTION);
        } else if (keycode == controls.down) {
            world.player.stopVerticalMovement(Constants.DOWN_DIRECTION);
        }
        return false;
    }
}

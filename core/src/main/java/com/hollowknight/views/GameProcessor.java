package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.hollowknight.models.Constants;
import com.hollowknight.models.settings.Controls;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.actors.modals.PauseModal;
import com.hollowknight.views.screens.MainMenuScreen;

public class GameProcessor implements InputProcessor {
    private final GameWorld game;

    public GameProcessor(GameWorld game) {
        this.game = game;
    }

    @Override
    public boolean keyDown(int keycode) {
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

        GameActionType action = controls.getAction(keycode);
        if (action != null) {
            game.player.doAction(action);
        }

        // Only check for and apply cheats if Ctrl is held
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            GameCheat cheat = controls.getCheat(keycode);

            if (cheat != null) {
                game.player.applyCheat(cheat);
            }
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        Controls controls = Settings.getInstance().getControls();
        if (keycode == controls.right) {
            game.player.stopMoving(Constants.RIGHT_DIRECTION);
        } else if (keycode == controls.left) {
            game.player.stopMoving(Constants.LEFT_DIRECTION);
        } else if (keycode == controls.focus) {
            game.player.stopFocus();
        } else if (keycode == controls.up) {
            game.player.stopVerticalMovement(Constants.UP_DIRECTION);
        } else if (keycode == controls.down) {
            game.player.stopVerticalMovement(Constants.DOWN_DIRECTION);
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}

package com.hollowknight.views;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.hollowknight.models.Constants;
import com.hollowknight.models.GameWorld;
import com.hollowknight.models.settings.Controls;
import com.hollowknight.models.settings.Settings;
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
        } else if (keycode == controls.right) {
            game.player.moveRight();
        } else if (keycode == controls.left) {
            game.player.moveLeft();
        } else if (keycode == controls.jump) {
            game.player.jump();
        } else if (keycode == controls.dash) {
            game.player.dash();
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

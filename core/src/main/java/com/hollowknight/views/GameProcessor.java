package com.hollowknight.views;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.hollowknight.models.GameWorld;
import com.hollowknight.views.actors.modals.PauseModal;
import com.hollowknight.views.screens.MainMenuScreen;

public class GameProcessor implements InputProcessor {
    private final GameWorld game;

    public GameProcessor(GameWorld game) {
        this.game = game;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE -> {
                // UiManager.setScreen(new MainMenuScreen());
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
            case Input.Keys.D -> game.player.movingRight = true;
            case Input.Keys.A -> game.player.movingLeft = true;
            case Input.Keys.SPACE -> game.player.jump();
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.D -> game.player.movingRight = false;
            case Input.Keys.A -> game.player.movingLeft = false;
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

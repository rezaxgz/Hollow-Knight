package com.hollowknight.views.screens;

import com.hollowknight.controller.GameController;
import com.hollowknight.models.GameWorld;
import com.hollowknight.views.GameRenderer;

public class GameScreen extends AbstractScreen {
    GameWorld world;
    private GameController controller;
    private GameRenderer renderer;

    public GameScreen(GameWorld world) {
        this.world = world;
        controller = new GameController(world);
        renderer = new GameRenderer(world);
    }

    @Override
    public void show() {
        super.show();
        renderer.show();
    }

    @Override
    public void render(float delta) {
        controller.update(delta);
        renderer.render();
        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        renderer.resize(width, height);
    }
}

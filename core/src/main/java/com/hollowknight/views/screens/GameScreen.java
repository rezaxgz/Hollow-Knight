package com.hollowknight.views.screens;

import com.hollowknight.controller.GameController;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.GameRenderer;

public class GameScreen extends AbstractScreen {
    GameWorld world;
    private GameController controller;
    private GameRenderer renderer;

    public GameScreen(GameWorld world) {
        this.world = world;
        controller = GameController.init(world);
        renderer = new GameRenderer(world);
    }

    @Override
    public void show() {
        super.show();
        renderer.show();
        GameAssetManager.menuBgm.stop();
    }

    @Override
    public void render(float delta) {
        float cappedDelta = Math.min(delta, 1 / 30f);
        controller.update(cappedDelta);
        renderer.render();
        super.render(cappedDelta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        renderer.resize(width, height);
    }
}

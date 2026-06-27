package com.hollowknight.views;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.hollowknight.models.world.GameWorld;

public class HUDRenderer {

    private final GameWorld world;
    private final ShapeRenderer shapeRenderer;

    public HUDRenderer(GameWorld world) {
        this.world = world;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void render(OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // TEMP HP BAR
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(
                camera.position.x - camera.viewportWidth / 2 + 20,
                camera.position.y + camera.viewportHeight / 2 - 40,
                200,
                20);

        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(
                camera.position.x - camera.viewportWidth / 2 + 20,
                camera.position.y + camera.viewportHeight / 2 - 40,
                world.player.getHealth() * 40,
                20);

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
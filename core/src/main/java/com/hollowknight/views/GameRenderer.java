package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.models.GameWorld;

public class GameRenderer {
    SpriteBatch batch;
    OrthographicCamera camera;
    ScreenViewport viewport;
    ShapeRenderer shapeRenderer;
    GameProcessor gameProcessor;
    Stage stage;
    GameWorld world;

    public GameRenderer(GameWorld world) {
        this.world = world;
    }

    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        shapeRenderer = new ShapeRenderer();
        stage = new Stage();

        gameProcessor = new GameProcessor(world);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(gameProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void render() {
        camera.position.set(world.player.position, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        AnimationType currentAnimation = world.player.currentAnimation;
        Animation<TextureRegion> animation = GameAssetManager.animationMap.get(currentAnimation);
        TextureRegion keyFrame = animation.getKeyFrame(world.player.stateTime);
        batch.draw(keyFrame,
                world.player.position.x, world.player.position.y,
                keyFrame.getRegionWidth() / 2f, 0,
                keyFrame.getRegionWidth(), keyFrame.getRegionHeight(),
                world.player.velocity.x > 0 ? -1 : 1, 1, 0);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 100, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 100);
        shapeRenderer.rect(world.player.position.x, world.player.position.y, 100, 100);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(-1000, -200, 2000, 200);
        shapeRenderer.end();

        stage.act();
    }
}

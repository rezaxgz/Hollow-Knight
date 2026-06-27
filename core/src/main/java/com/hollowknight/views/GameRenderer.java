package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.models.Constants;
import com.hollowknight.models.GameWorld;
import com.hollowknight.models.PlayerState;

public class GameRenderer {
    SpriteBatch batch;
    OrthographicCamera camera;
    ScreenViewport viewport;
    ShapeRenderer shapeRenderer;
    GameProcessor gameProcessor;
    Stage stage;
    GameWorld world;

    private OrthogonalTiledMapRenderer mapRenderer;

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

        mapRenderer = new OrthogonalTiledMapRenderer(world.map);
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void render() {
        camera.position.set(world.player.position, 0);
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        PlayerState currentAnimation = world.player.state;
        Animation<TextureRegion> animation = GameAssetManager.animationMap.get(currentAnimation);
        TextureRegion keyFrame = animation.getKeyFrame(world.player.stateTime);
        float spriteWidth = keyFrame.getRegionWidth();
        float spriteHeight = keyFrame.getRegionHeight();
        float xOffset = (spriteWidth - Constants.PLAYER_HITBOX_WIDTH) / 2f;
        batch.draw(keyFrame,
                world.player.position.x - xOffset, // Adjusted X to co-locate
                world.player.position.y, // Keep Y co-located with ground
                spriteWidth / 2f, 0, // Center of origin for flipping
                spriteWidth, spriteHeight,
                -world.player.getDirection(), 1, 0); // Flip horizontally if moving left
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 100, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 100);
        shapeRenderer.rect(world.player.position.x, world.player.position.y, Constants.PLAYER_HITBOX_WIDTH,
                Constants.PLAYER_HITBOX_HEIGHT);
        shapeRenderer.end();

        stage.act();
    }
}

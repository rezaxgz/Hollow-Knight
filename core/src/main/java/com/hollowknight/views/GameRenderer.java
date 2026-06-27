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
import com.hollowknight.models.player.PlayerState;
import com.hollowknight.models.world.GameWorld;

public class GameRenderer {
    SpriteBatch batch;
    OrthographicCamera camera;
    ScreenViewport viewport;
    ShapeRenderer shapeRenderer;
    GameProcessor gameProcessor;
    Stage stage;
    GameWorld world;

    private OrthogonalTiledMapRenderer mapRenderer;

    private HUDRenderer hudRenderer;
    private OrthographicCamera hudCamera;

    // Arrays to hold the layer indices
    private int[] backgroundLayers;
    private int[] foregroundLayers;

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

        // Define your layers here.
        backgroundLayers = new int[] {
                world.map.getLayers().getIndex("background"),
                world.map.getLayers().getIndex("main")
        };

        foregroundLayers = new int[] {
                world.map.getLayers().getIndex("foreground")
        };

        hudRenderer = new HUDRenderer(world);
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void render() {
        camera.position.set(world.player.position, 0);
        camera.update();

        mapRenderer.setView(camera);

        // 1. Render Background and Main map layers FIRST
        mapRenderer.render(backgroundLayers);

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 2. Render the Player
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

        // 3. Render the Foreground map layer OVER the player
        mapRenderer.render(foregroundLayers);

        // 4. render HUD
        hudCamera.update();
        hudRenderer.render(hudCamera);

        // 5. Render Debug Shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 100, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 100);
        shapeRenderer.rect(world.player.position.x, world.player.position.y, Constants.PLAYER_HITBOX_WIDTH,
                Constants.PLAYER_HITBOX_HEIGHT);
        shapeRenderer.end();

        // 6. Update and Draw Stage (UI)
        stage.act();
        stage.draw();
    }
}
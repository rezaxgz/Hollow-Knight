package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.models.Constants;
import com.hollowknight.models.enemies.CrystalGuardian;
import com.hollowknight.models.enemies.Enemy;
import com.hollowknight.models.player.PlayerAnimation;
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

    private float mapWidth;
    private float mapHeight;

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

        gameProcessor = new GameProcessor();
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(gameProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);

        mapRenderer = new OrthogonalTiledMapRenderer(world.map);

        // Extract map dimensions from properties
        int tileWidth = world.map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = world.map.getProperties().get("tileheight", Integer.class);
        int mapWidthInTiles = world.map.getProperties().get("width", Integer.class);
        int mapHeightInTiles = world.map.getProperties().get("height", Integer.class);

        this.mapWidth = mapWidthInTiles * tileWidth;
        this.mapHeight = mapHeightInTiles * tileHeight;

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

    private void setCameraPosition() {
        // Track the player first
        camera.position.set(world.player.position, 0);

        // --- CAMERA CLAMPING LOGIC ---
        float cameraHalfWidth = camera.viewportWidth / 2f;
        float cameraHalfHeight = camera.viewportHeight / 2f;

        // Clamp X: Ensure camera doesn't spill past left/right edges
        if (mapWidth > camera.viewportWidth) {
            camera.position.x = MathUtils.clamp(camera.position.x, cameraHalfWidth, mapWidth - cameraHalfWidth);
        } else {
            camera.position.x = mapWidth / 2f; // Center map if it's smaller than screen width
        }

        // Clamp Y: Ensure camera doesn't spill past bottom/top edges
        if (mapHeight > camera.viewportHeight) {
            camera.position.y = MathUtils.clamp(camera.position.y, cameraHalfHeight, mapHeight - cameraHalfHeight);
        } else {
            camera.position.y = mapHeight / 2f; // Center map if it's smaller than screen height
        }
        // ------------------------------
    }

    public void render() {
        setCameraPosition();
        camera.update();

        mapRenderer.setView(camera);

        // 1. Render Background and Main map layers FIRST
        mapRenderer.render(backgroundLayers);

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();

        // 2 Render enemies and player
        renderEnemies(batch);
        renderPlayer(batch);
        renderLasers(batch);

        batch.end();

        // 3. Render the Foreground map layer OVER the player
        mapRenderer.render(foregroundLayers);

        // 4. render HUD
        hudCamera.update();
        hudRenderer.render(hudCamera);

        // 5. Render Debug Shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Render player hitboxe
        shapeRenderer.setColor(Color.GREEN);
        renderPlayerHitBox(shapeRenderer);
        // Render enemy hitboxes
        shapeRenderer.setColor(Color.RED);
        renderEnemyHitBoxes(shapeRenderer);
        // Render player attack hitboxes
        shapeRenderer.setColor(Color.ORANGE);
        renderPlayerAttackHitboxe(shapeRenderer);

        shapeRenderer.end();

        // 6. Update and Draw Stage (UI)
        stage.act();
        stage.draw();
    }

    private void renderEnemies(SpriteBatch batch) {
        for (Enemy enemy : world.enemies) {

            Animation<TextureRegion> animation = GameAssetManager.enemyAnimationMap.get(enemy.animation);

            TextureRegion frame = animation.getKeyFrame(enemy.animationTime);
            float spriteWidth = frame.getRegionWidth();
            float spriteHeight = frame.getRegionHeight();

            // Simple camera culling
            if (enemy.position.x + spriteWidth < camera.position.x - viewport.getWorldWidth() / 2f ||
                    enemy.position.x > camera.position.x + viewport.getWorldWidth() / 2f ||
                    enemy.position.y + spriteHeight < camera.position.y - viewport.getWorldHeight() / 2f ||
                    enemy.position.y > camera.position.y + viewport.getWorldHeight() / 2f) {
                continue;
            }

            float xOffset = (spriteWidth - Constants.GROUND_ENEMY_HITBOX_WIDTH) / 2f;
            batch.draw(
                    frame,
                    enemy.position.x - xOffset,
                    enemy.position.y,
                    spriteWidth / 2f,
                    0,
                    spriteWidth,
                    spriteHeight,
                    -enemy.facingDirection, // 1 = right, -1 = left
                    1,
                    0);
        }
    }

    private void renderEnemyHitBoxes(ShapeRenderer shapeRenderer) {
        for (Enemy enemy : world.enemies) {
            if (enemy instanceof CrystalGuardian) {
                Rectangle laserBounds = ((CrystalGuardian) enemy).getActiveLaserBounds(world.solidBlocks);
                if (laserBounds != null) {
                    shapeRenderer.rect(laserBounds.x, laserBounds.y, laserBounds.width, laserBounds.height);
                }
            }

            Animation<TextureRegion> animation = GameAssetManager.enemyAnimationMap.get(enemy.animation);

            TextureRegion frame = animation.getKeyFrame(enemy.animationTime);

            float width = frame.getRegionWidth();
            float height = frame.getRegionHeight();

            // Simple camera culling
            if (enemy.position.x + width < camera.position.x - viewport.getWorldWidth() / 2f ||
                    enemy.position.x > camera.position.x + viewport.getWorldWidth() / 2f ||
                    enemy.position.y + height < camera.position.y - viewport.getWorldHeight() / 2f ||
                    enemy.position.y > camera.position.y + viewport.getWorldHeight() / 2f) {
                continue;
            }

            Rectangle bounds = enemy.getBounds();
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private void renderLasers(SpriteBatch batch) {
        for (Enemy enemy : world.enemies) {
            if (enemy instanceof CrystalGuardian) {
                CrystalGuardian guardian = (CrystalGuardian) enemy;
                Rectangle laserBounds = guardian.getActiveLaserBounds(world.solidBlocks);

                if (laserBounds != null) {
                    // Enable Additive Blending: Black becomes transparent, colors glow!
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

                    // Draw the laser stretched over the calculated bounds
                    batch.draw(GameAssetManager.laserTexture[guardian.getLaserAnimationIndex()],
                            laserBounds.x,
                            laserBounds.y,
                            laserBounds.width,
                            laserBounds.height);

                    // Reset blending back to normal so the rest of the game renders correctly
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                    batch.draw(GameAssetManager.laserStartTexture, guardian.getLaserCircleStartX(),
                            guardian.getLaserCircleStartY());
                }
            }
        }
    }

    private void renderPlayer(SpriteBatch batch) {
        if (world.player.shouldFlash())
            return;

        PlayerAnimation currentAnimation = world.player.animation;
        Animation<TextureRegion> animation = GameAssetManager.playerAnimationMap.get(currentAnimation);
        TextureRegion keyFrame = animation.getKeyFrame(world.player.animationTime);
        float spriteWidth = keyFrame.getRegionWidth();
        float spriteHeight = keyFrame.getRegionHeight();
        float xOffset = (spriteWidth - Constants.PLAYER_HITBOX_WIDTH) / 2f;
        batch.draw(keyFrame,
                world.player.position.x - xOffset, // Adjusted X to co-locate
                world.player.position.y, // Keep Y co-located with ground
                spriteWidth / 2f, 0, // Center of origin for flipping
                spriteWidth, spriteHeight,
                -world.player.getDirection(), 1, 0);
    }

    private void renderPlayerHitBox(ShapeRenderer shapeRenderer) {
        Rectangle bounds = world.player.getBounds();
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void renderPlayerAttackHitboxe(ShapeRenderer shapeRenderer) {
        Rectangle bounds = world.player.getAttackHitbox();
        if (bounds == null)
            return;
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
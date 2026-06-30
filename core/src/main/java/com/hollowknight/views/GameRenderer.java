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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.PlayerAnimation;
import com.hollowknight.models.player.enemies.GroundEnemy;
import com.hollowknight.models.player.enemies.HuskHornHead;
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

        batch.begin();

        // Render ground enemies
        renderGroundEnemies(batch);
        // Render special enemies
        renderHornheads(batch);

        // 2. Render the Player
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
        // Render enemy hitboxes
        shapeRenderer.setColor(Color.RED);
        renderGroundEnemyHitBoxes(shapeRenderer);
        renderHornheadHitBoxes(shapeRenderer);

        shapeRenderer.end();

        // 6. Update and Draw Stage (UI)
        stage.act();
        stage.draw();
    }

    private void renderGroundEnemies(SpriteBatch batch) {
        for (GroundEnemy enemy : world.groundEnemies) {

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

            float xOffset = (spriteWidth - Constants.CRAWLID_HITBOX_WIDTH) / 2f;
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

    private void renderGroundEnemyHitBoxes(ShapeRenderer shapeRenderer) {
        for (GroundEnemy enemy : world.groundEnemies) {

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

            shapeRenderer.rect(enemy.position.x, enemy.position.y, enemy.type.width, enemy.type.height);
        }
    }

    private void renderHornheads(SpriteBatch batch) {
        for (HuskHornHead enemy : world.hornHeads) {

            // Ensure the map name matches what you use in GameAssetManager for
            // EnemyAnimations
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

            // Offset to center the sprite horizontally over the hitbox
            float xOffset = (spriteWidth - Constants.HORNHEAD_HITBOX_WIDTH) / 2f;

            batch.draw(
                    frame,
                    enemy.position.x - xOffset,
                    enemy.position.y,
                    spriteWidth / 2f, // Origin X for flipping
                    0, // Origin Y
                    spriteWidth,
                    spriteHeight,
                    -enemy.facingDirection, // -1 flips it when walking left
                    1,
                    0);
        }
    }

    private void renderHornheadHitBoxes(ShapeRenderer shapeRenderer) {
        for (HuskHornHead enemy : world.hornHeads) {

            // Get dimensions for culling calculation
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
            // Draw actual hitbox dimensions
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}
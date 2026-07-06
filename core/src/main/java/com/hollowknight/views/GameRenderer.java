package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.models.Constants;
import com.hollowknight.models.enemies.CrystalGuardian;
import com.hollowknight.models.enemies.Enemy;
import com.hollowknight.models.player.PlayerAnimation;
import com.hollowknight.models.player.PlayerEffect;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.models.world.PlayerProjectile;

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
        Vector2 pos = world.player.position.cpy();

        camera.position.set(pos, 0);

        float cameraHalfWidth = camera.viewportWidth / 2f;
        float cameraHalfHeight = camera.viewportHeight / 2f;

        // Clamp X: Ensure camera doesn't spill past left/right edges of the whole map
        if (mapWidth > camera.viewportWidth) {
            camera.position.x = MathUtils.clamp(camera.position.x, cameraHalfWidth, mapWidth - cameraHalfWidth);
        } else {
            camera.position.x = mapWidth / 2f;
        }

        // Clamp Y: Ensure camera doesn't spill past bottom/top edges of the whole map
        if (mapHeight > camera.viewportHeight) {
            camera.position.y = MathUtils.clamp(camera.position.y, cameraHalfHeight, mapHeight - cameraHalfHeight);
        } else {
            camera.position.y = mapHeight / 2f;
        }

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
        renderPlayerEffects(batch);
        renderProjectiles(batch);
        renderLasers(batch);
        renderZote(batch);

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
        renderProjectileHitboxes(shapeRenderer);
        // Render player attack hitboxes
        shapeRenderer.setColor(Color.ORANGE);
        renderPlayerAttackHitboxe(shapeRenderer);

        shapeRenderer.end();

        renderZoteDialouges(batch);

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

            float xOffset = (spriteWidth - enemy.getBounds().width) / 2f;
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

    private void renderPlayerEffects(SpriteBatch batch) {
        PlayerEffect activeEffect = null;

        if (world.player.combatState == com.hollowknight.models.player.states.CombatState.SCREAM) {
            activeEffect = PlayerEffect.SOUL_SCREAM;
        } else if (world.player.combatState == com.hollowknight.models.player.states.CombatState.CAST) {
            activeEffect = PlayerEffect.BLAST;
        }

        if (activeEffect != null) {
            Animation<TextureRegion> animation = GameAssetManager.playerEffectAnimationMap.get(activeEffect);
            TextureRegion keyFrame = animation.getKeyFrame(world.player.animationTime);

            float spriteWidth = keyFrame.getRegionWidth();
            float spriteHeight = keyFrame.getRegionHeight();
            float xOffset = (spriteWidth - Constants.PLAYER_HITBOX_WIDTH) / 2f;

            float x = world.player.position.x - xOffset + activeEffect.xOffset;
            float y = world.player.position.y + activeEffect.yOffset;

            // Flip horizontal if it's BLAST
            float scaleX = (activeEffect == PlayerEffect.BLAST) ? -world.player.getDirection() : 1f;

            batch.draw(keyFrame,
                    x, y,
                    spriteWidth / 2f, spriteHeight / 2f, // Origin for flipping
                    spriteWidth, spriteHeight,
                    scaleX, 1, 0);
        }
    }

    private void renderProjectiles(SpriteBatch batch) {
        for (PlayerProjectile proj : world.projectiles) {

            // Swap to explosion sprite if it hit something
            PlayerEffect effectType = proj.isExploding ? PlayerEffect.SOUL_BALL_END : PlayerEffect.SOUL_BALL;
            Animation<TextureRegion> animation = GameAssetManager.playerEffectAnimationMap.get(effectType);
            TextureRegion keyFrame = animation.getKeyFrame(proj.animationTime);

            float spriteWidth = keyFrame.getRegionWidth();
            float spriteHeight = keyFrame.getRegionHeight();

            float xOffset = (spriteWidth - Constants.PROJECTILE_SIZE) / 2f;
            float yOffset = (spriteHeight - Constants.PROJECTILE_SIZE) / 2f;

            batch.draw(
                    keyFrame,
                    proj.position.x - xOffset,
                    proj.position.y - yOffset,
                    spriteWidth / 2f, spriteHeight / 2f, // Center origin
                    spriteWidth, spriteHeight,
                    proj.direction, // Flip sprite depending on travel direction
                    1, 0);
        }
    }

    private void renderPlayerHitBox(ShapeRenderer shapeRenderer) {
        Rectangle bounds = world.player.getBounds();
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void renderPlayerAttackHitboxe(ShapeRenderer shapeRenderer) {
        // Draw normal slash hitbox
        Rectangle attackBounds = world.player.getAttackHitbox();
        if (attackBounds != null) {
            shapeRenderer.rect(attackBounds.x, attackBounds.y, attackBounds.width, attackBounds.height);
        }

        // Draw Soul Scream hitbox
        Rectangle screamBounds = world.player.getScreamHitbox();
        if (screamBounds != null) {
            shapeRenderer.rect(screamBounds.x, screamBounds.y, screamBounds.width, screamBounds.height);
        }
    }

    private void renderProjectileHitboxes(ShapeRenderer shapeRenderer) {
        for (PlayerProjectile proj : world.projectiles) {
            if (proj.isExploding)
                continue;
            Rectangle bounds = proj.getBounds();
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private void renderZote(SpriteBatch batch) {
        if (world.zote == null)
            return;

        Animation<TextureRegion> animation = GameAssetManager.zoteAnimationMap.get(world.zote.animation);
        TextureRegion frame = animation.getKeyFrame(world.zote.animationTime);

        // Draw Zote at his spawned coordinates
        batch.draw(frame, world.zote.position.x - frame.getRegionWidth() / 2, world.zote.position.y);
    }

    private void renderZoteDialouges(SpriteBatch batch) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        com.badlogic.gdx.graphics.g2d.BitmapFont font = GameAssetManager.hollowSkin.getFont("Hollowfont");

        if (world.zote != null) {
            if (world.zote.isTalking) {

                // 1. Determine which text to draw based on our new flag
                String textToDraw;
                if (!world.zote.hasCompletedFirstDialogue) {
                    textToDraw = world.zote.dialogues[world.zote.dialogueIndex];
                } else {
                    textToDraw = world.zote.rules[world.zote.currentRuleIndex];
                }

                // 2. Draw the dialogue box at the top of the screen
                font.getData().setScale(1.5f);
                font.draw(batch,
                        textToDraw,
                        0,
                        Gdx.graphics.getHeight() - 130f,
                        Gdx.graphics.getWidth(),
                        Align.center,
                        true); // CHANGED TO TRUE: This wraps long Precepts nicely!
                font.getData().setScale(1f); // Reset scale

            } else if (world.zote.playerIsClose) {
                // Draw the interaction guide near the middle/bottom of the screen
                Texture button = GameAssetManager.eButton;
                float width = 677f / 3f;
                float height = 369f / 3f;

                batch.draw(button, Gdx.graphics.getWidth() / 2f - width / 2f,
                        Gdx.graphics.getHeight() / 2f - 300f, width, height);

                font.getData().setScale(1f);
                font.draw(batch,
                        "Press E to interact",
                        0,
                        Gdx.graphics.getHeight() / 2f - 300f,
                        Gdx.graphics.getWidth(),
                        Align.center,
                        false);
            }
        }
        batch.end();
    }
}
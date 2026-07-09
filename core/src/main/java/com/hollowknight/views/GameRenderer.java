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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.models.Constants;
import com.hollowknight.models.enemies.CrystalGuardian;
import com.hollowknight.models.enemies.Enemy;
import com.hollowknight.models.enemies.EnemyAnimations;
import com.hollowknight.models.enemies.FalseKnight;
import com.hollowknight.models.player.ActiveEffect;
import com.hollowknight.models.player.CharmType;
import com.hollowknight.models.player.PlayerAnimation;
import com.hollowknight.models.player.PlayerEffectAnimation;
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
    private int[] backgroundLayers;
    private int[] foregroundLayers;

    private boolean isCameraInitialized = false;

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

        int tileWidth = world.map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = world.map.getProperties().get("tileheight", Integer.class);
        int mapWidthInTiles = world.map.getProperties().get("width", Integer.class);
        int mapHeightInTiles = world.map.getProperties().get("height", Integer.class);

        this.mapWidth = mapWidthInTiles * tileWidth;
        this.mapHeight = mapHeightInTiles * tileHeight;

        backgroundLayers = new int[] {
                world.map.getLayers().getIndex("background"),
                world.map.getLayers().getIndex("main")
        };

        foregroundLayers = new int[] {
                world.map.getLayers().getIndex("foreground")
        };

        hudRenderer = new HUDRenderer(world);
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void setCameraPosition() {
        Rectangle currentRegion = null;
        Rectangle playerBounds = world.player.getBounds();

        // Use the center of the player to determine region overlap
        float playerCenterX = playerBounds.x + playerBounds.width / 2f;
        float playerCenterY = playerBounds.y + playerBounds.height / 2f;

        // Find which region the player is currently in
        for (Rectangle region : world.regions) {
            if (region.contains(playerCenterX, playerCenterY)) {
                currentRegion = region;
                break;
            }
        }

        // Define bounding limits based on the current region, fallback to the entire
        // map
        float minX = currentRegion != null ? currentRegion.x : 0;
        float minY = currentRegion != null ? currentRegion.y : 0;
        float maxX = currentRegion != null ? currentRegion.x + currentRegion.width : mapWidth;
        float maxY = currentRegion != null ? currentRegion.y + currentRegion.height : mapHeight;

        float cameraHalfWidth = camera.viewportWidth / 2f;
        float cameraHalfHeight = camera.viewportHeight / 2f;

        float targetX = world.player.position.x;
        float targetY = world.player.position.y;

        // Clamp target X to region boundaries
        if (maxX - minX > camera.viewportWidth) {
            targetX = MathUtils.clamp(targetX, minX + cameraHalfWidth, maxX - cameraHalfWidth);
        } else {
            // Center the camera horizontally if the region is smaller than the viewport
            targetX = minX + (maxX - minX) / 2f;
        }

        // Clamp target Y to region boundaries
        if (maxY - minY > camera.viewportHeight) {
            targetY = MathUtils.clamp(targetY, minY + cameraHalfHeight, maxY - cameraHalfHeight);
        } else {
            // Center the camera vertically if the region is smaller than the viewport
            targetY = minY + (maxY - minY) / 2f;
        }

        // Apply smooth camera follow or snap instantly on the very first frame
        if (!isCameraInitialized) {
            camera.position.set(targetX, targetY, 0);
            isCameraInitialized = true;
        } else {
            float lerpSpeed = 5.0f;
            camera.position.x += (targetX - camera.position.x) * lerpSpeed * Gdx.graphics.getDeltaTime();
            camera.position.y += (targetY - camera.position.y) * lerpSpeed * Gdx.graphics.getDeltaTime();
        }

        if (world.cameraShakeTimer > 0) {
            // Apply randomized offset based on current intensity
            camera.position.x += MathUtils.random(-world.cameraShakeIntensity, world.cameraShakeIntensity);
            camera.position.y += MathUtils.random(-world.cameraShakeIntensity, world.cameraShakeIntensity);
        }
    }

    public void renderWorld() {
        setCameraPosition();
        camera.update();
        mapRenderer.setView(camera);

        mapRenderer.render(backgroundLayers);

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.begin();
        renderEnemies(batch);
        renderPlayer(batch);
        renderPlayerEffects(batch);
        renderProjectiles(batch);
        renderLasers(batch);
        renderZote(batch);
        renderShockwaves(batch);
        renderBossFightEffects(batch);
        batch.end();

        mapRenderer.render(foregroundLayers);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        renderPlayerHitBox(shapeRenderer);

        shapeRenderer.setColor(Color.RED);
        renderEnemyHitBoxes(shapeRenderer);
        renderProjectileHitboxes(shapeRenderer);

        shapeRenderer.setColor(Color.ORANGE);
        renderPlayerAttackHitboxe(shapeRenderer);
        shapeRenderer.end();
    }

    public void renderUI(float delta) {
        hudCamera.update();
        hudRenderer.render(hudCamera);

        renderZoteDialouges(batch);

        stage.act(delta);
        stage.draw();
    }

    private void renderBossFightEffects(SpriteBatch batch) {
        Rectangle bossDoor = world.bossDoor;
        // When progress is 0, visual Y is shifted up by its entire height (hidden in
        // ceiling)
        // When progress is 1, visual Y is exactly doorBounds.y (resting on ground)
        float animatedY = bossDoor.y + (bossDoor.height * (1f - world.gateDropProgress));

        batch.draw(GameAssetManager.gateTexture,
                bossDoor.x,
                animatedY,
                bossDoor.width,
                bossDoor.height);
    }

    private void renderShockwaves(SpriteBatch batch) {
        for (Enemy enemy : world.enemies) {
            if (enemy instanceof FalseKnight) {
                FalseKnight fk = (FalseKnight) enemy;

                for (FalseKnight.Shockwave wave : fk.shockwaves) {
                    Animation<TextureRegion> animation = GameAssetManager.enemyAnimationMap
                            .get(EnemyAnimations.FALSE_KNIGHT_SHOCKWAVE);

                    // Assumes your Shockwave class has an animationTime tracker.
                    // If it doesn't, you will need to add one to FalseKnight.Shockwave and update
                    // it in your GameProcessor.
                    TextureRegion frame = animation.getKeyFrame(wave.lifetime);

                    float spriteWidth = frame.getRegionWidth();
                    float spriteHeight = frame.getRegionHeight();

                    float x = wave.velocityX < 0f ? wave.bounds.x - 10
                            : wave.bounds.x + wave.bounds.width - spriteWidth + 10;

                    // Assumes you want to flip the sprite based on travel direction.
                    // If your wave stores direction (e.g., 1 for right, -1 for left), replace the
                    // '1' scaleX parameter below with wave.direction
                    batch.draw(frame,
                            x,
                            wave.bounds.y + spriteHeight / 2 - 15,
                            spriteWidth / 2f, spriteHeight / 2f,
                            spriteWidth, spriteHeight,
                            wave.getDir(), 2, 0); // Change the first '1' to -1 or wave.direction to flip
                                                  // horizontally
                }
            }
        }
    }

    private void renderEnemies(SpriteBatch batch) {
        for (Enemy enemy : world.enemies) {
            Animation<TextureRegion> animation = GameAssetManager.enemyAnimationMap.get(enemy.animation);
            TextureRegion frame = animation.getKeyFrame(enemy.animationTime);
            float spriteWidth = frame.getRegionWidth();
            float spriteHeight = frame.getRegionHeight();

            if (enemy.position.x + spriteWidth < camera.position.x - viewport.getWorldWidth() / 2f ||
                    enemy.position.x > camera.position.x + viewport.getWorldWidth() / 2f ||
                    enemy.position.y + spriteHeight < camera.position.y - viewport.getWorldHeight() / 2f ||
                    enemy.position.y > camera.position.y + viewport.getWorldHeight() / 2f) {
                continue;
            }

            float xOffset = (spriteWidth - enemy.getBounds().width) / 2f;
            float yOffset = (enemy instanceof FalseKnight) ? -30 : 0;
            batch.draw(frame, enemy.position.x - xOffset, enemy.position.y + yOffset,
                    spriteWidth / 2f, 0, spriteWidth, spriteHeight,
                    -enemy.facingDirection, 1, 0);
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

            if (enemy instanceof FalseKnight) {
                FalseKnight fk = (FalseKnight) enemy;

                // 1. Render Melee Attack Hitbox (Magenta)
                Rectangle mace = fk.getActiveAttackHitbox();
                if (mace != null) {
                    shapeRenderer.setColor(Color.MAGENTA);
                    shapeRenderer.rect(mace.x, mace.y, mace.width, mace.height);
                }

                // 2. Render Floor Shockwaves (Yellow)
                shapeRenderer.setColor(Color.YELLOW);
                for (FalseKnight.Shockwave wave : fk.shockwaves) {
                    shapeRenderer.rect(wave.bounds.x, wave.bounds.y, wave.bounds.width, wave.bounds.height);
                }

                // Reset color back to Red for the main body hitboxes
                shapeRenderer.setColor(Color.RED);
            }

            Animation<TextureRegion> animation = GameAssetManager.enemyAnimationMap.get(enemy.animation);
            TextureRegion frame = animation.getKeyFrame(enemy.animationTime);
            float width = frame.getRegionWidth();
            float height = frame.getRegionHeight();

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
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    batch.draw(GameAssetManager.laserTexture[guardian.getLaserAnimationIndex()],
                            laserBounds.x, laserBounds.y, laserBounds.width, laserBounds.height);
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
        batch.draw(keyFrame, world.player.position.x - xOffset, world.player.position.y,
                spriteWidth / 2f, 0, spriteWidth, spriteHeight,
                -world.player.getDirection(), 1, 0);
    }

    private void renderPlayerEffects(SpriteBatch batch) {
        for (ActiveEffect effect : world.player.activeEffects) {
            Animation<TextureRegion> animation = GameAssetManager.playerEffectAnimationMap.get(effect.type);
            if (animation == null)
                continue;

            TextureRegion keyFrame = animation.getKeyFrame(effect.timer);
            float spriteWidth = keyFrame.getRegionWidth();
            float spriteHeight = keyFrame.getRegionHeight();

            // Calculate base offset
            float baseOffsetX = (spriteWidth - Constants.PLAYER_HITBOX_WIDTH) / 2f;

            // Apply direction to the effect's specific xOffset
            float x = world.player.position.x - baseOffsetX + (effect.type.xOffset * effect.direction);
            float y = world.player.position.y + effect.type.yOffset;

            // Preserve original Blast inverted scaling, otherwise scale based on direction
            float scaleX = (effect.type == PlayerEffectAnimation.BLAST) ? -effect.direction : effect.direction;

            batch.draw(keyFrame, x, y, spriteWidth / 2f, spriteHeight / 2f, spriteWidth, spriteHeight,
                    scaleX * effect.type.sclaeX, 1, 0);
        }
    }

    private void renderProjectiles(SpriteBatch batch) {
        for (PlayerProjectile proj : world.projectiles) {
            PlayerEffectAnimation effectType = world.player.hasCharm(CharmType.VOID_HEART)
                    ? (proj.isExploding ? PlayerEffectAnimation.SHADOW_BALL_END : PlayerEffectAnimation.SHADOW_BALL)
                    : (proj.isExploding ? PlayerEffectAnimation.SOUL_BALL_END
                            : PlayerEffectAnimation.SOUL_BALL);
            Animation<TextureRegion> animation = GameAssetManager.playerEffectAnimationMap.get(effectType);
            TextureRegion keyFrame = animation.getKeyFrame(proj.animationTime);
            float spriteWidth = keyFrame.getRegionWidth();
            float spriteHeight = keyFrame.getRegionHeight();
            float xOffset = (spriteWidth - Constants.PROJECTILE_SIZE) * 0.75f;
            float yOffset = (spriteHeight - Constants.PROJECTILE_SIZE) / 2f;
            batch.draw(keyFrame, proj.position.x - xOffset, proj.position.y - yOffset,
                    spriteWidth / 2f, spriteHeight / 2f, spriteWidth, spriteHeight, proj.direction, 1, 0);
        }
    }

    private void renderPlayerHitBox(ShapeRenderer shapeRenderer) {
        Rectangle bounds = world.player.getBounds();
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void renderPlayerAttackHitboxe(ShapeRenderer shapeRenderer) {
        Rectangle attackBounds = world.player.getAttackHitbox();
        if (attackBounds != null) {
            shapeRenderer.rect(attackBounds.x, attackBounds.y, attackBounds.width, attackBounds.height);
        }
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
        batch.draw(frame, world.zote.position.x - frame.getRegionWidth() / 2, world.zote.position.y);
    }

    private void renderZoteDialouges(SpriteBatch batch) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        com.badlogic.gdx.graphics.g2d.BitmapFont font = GameAssetManager.hollowSkin.getFont("Hollowfont");

        if (world.zote != null) {
            if (world.zote.isTalking) {
                String textToDraw = !world.zote.hasCompletedFirstDialogue
                        ? world.zote.dialogues[world.zote.dialogueIndex]
                        : world.zote.rules[world.zote.currentRuleIndex];

                font.getData().setScale(1.5f);
                font.draw(batch, textToDraw, 0, Gdx.graphics.getHeight() - 130f,
                        Gdx.graphics.getWidth(), Align.center, true);
                font.getData().setScale(1f);
            } else if (world.zote.playerIsClose) {
                Texture button = GameAssetManager.eButton;
                float width = 677f / 3f;
                float height = 369f / 3f;
                batch.draw(button, Gdx.graphics.getWidth() / 2f - width / 2f,
                        Gdx.graphics.getHeight() / 2f - 300f, width, height);
                font.getData().setScale(1f);
                font.draw(batch, "Press E to interact", 0,
                        Gdx.graphics.getHeight() / 2f - 300f, Gdx.graphics.getWidth(), Align.center, false);
            }
        }
        batch.end();
    }
}
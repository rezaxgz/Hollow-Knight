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
import com.hollowknight.models.enums.GameRegion;
import com.hollowknight.models.player.ActiveEffect;
import com.hollowknight.models.player.CharmType;
import com.hollowknight.models.player.PlayerAnimation;
import com.hollowknight.models.player.PlayerEffectAnimation;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.models.world.PlayerProjectile;
import com.hollowknight.views.effects.CrossroadsDustEffect;
import com.hollowknight.views.effects.GreenpathAmbientEffect;

public class GameRenderer {

    // --- Core Rendering Components ---
    SpriteBatch batch;
    OrthographicCamera camera;
    ScreenViewport viewport;
    ShapeRenderer shapeRenderer;
    Stage stage;

    // --- Game Logic References ---
    GameWorld world;
    GameProcessor gameProcessor;

    // --- Map & Layers ---
    private OrthogonalTiledMapRenderer mapRenderer;
    private float mapWidth;
    private float mapHeight;
    private int[] backgroundLayers;
    private int[] foregroundLayers;

    // --- UI & HUD ---
    private HUDRenderer hudRenderer;
    private OrthographicCamera hudCamera;

    // --- Visual Effects & Flags ---
    private CrossroadsDustEffect crossroadsDustEffect;
    private boolean crossroadsDustWasActive = false;
    private GreenpathAmbientEffect greenpathAmbientEffect;
    private boolean greenpathEffectWasActive = false;
    private boolean isCameraInitialized = false;

    // --- Initialization & Lifecycle ---
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
        crossroadsDustEffect = new CrossroadsDustEffect();
        greenpathAmbientEffect = new GreenpathAmbientEffect();

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

    public void dispose() {
        if (crossroadsDustEffect != null) {
            crossroadsDustEffect.dispose();
            crossroadsDustEffect = null;
        }
        if (greenpathAmbientEffect != null) {
            greenpathAmbientEffect.dispose();
            greenpathAmbientEffect = null;
        }
    }

    // --- Core Render Loops ---
    public void renderWorld(float delta) {
        setCameraPosition();
        camera.update();

        float tilePadding = 100;
        float viewX = camera.position.x - (camera.viewportWidth / 2f) - tilePadding;
        float viewY = camera.position.y - (camera.viewportHeight / 2f) - tilePadding;
        float viewWidth = camera.viewportWidth + (tilePadding * 2f);
        float viewHeight = camera.viewportHeight + (tilePadding * 2f);

        mapRenderer.setView(camera.combined, viewX, viewY, viewWidth, viewHeight);

        updateGreenpathAmbient(delta);
        mapRenderer.render(backgroundLayers);

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        renderGreenpathBackground();

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
        renderCrossroadsDust(delta);
        renderGreenpathForeground();

        if (Constants.flag) {
            renderDebugHitboxes();
        }
    }

    public void renderUI(float delta) {
        hudCamera.update();
        hudRenderer.render(hudCamera);
        renderZoteDialouges(batch);
        stage.act(delta);
        stage.draw();
    }

    // --- Camera Management ---
    private void setCameraPosition() {
        Rectangle currentRegion = null;
        Rectangle playerBounds = world.player.getBounds();

        float playerCenterX = playerBounds.x + playerBounds.width / 2f;
        float playerCenterY = playerBounds.y + playerBounds.height / 2f;

        for (Rectangle region : world.regionBounds.values()) {
            if (region.contains(playerCenterX, playerCenterY)) {
                currentRegion = region;
                break;
            }
        }

        float minX = currentRegion != null ? currentRegion.x : 0;
        float minY = currentRegion != null ? currentRegion.y : 0;
        float maxX = currentRegion != null ? currentRegion.x + currentRegion.width : mapWidth;
        float maxY = currentRegion != null ? currentRegion.y + currentRegion.height : mapHeight;

        float cameraHalfWidth = camera.viewportWidth / 2f;
        float cameraHalfHeight = camera.viewportHeight / 2f;

        float targetX = world.player.position.x;
        float targetY = world.player.position.y;

        if (maxX - minX > camera.viewportWidth) {
            targetX = MathUtils.clamp(targetX, minX + cameraHalfWidth, maxX - cameraHalfWidth);
        } else {
            targetX = minX + (maxX - minX) / 2f;
        }

        if (maxY - minY > camera.viewportHeight) {
            targetY = MathUtils.clamp(targetY, minY + cameraHalfHeight, maxY - cameraHalfHeight);
        } else {
            targetY = minY + (maxY - minY) / 2f;
        }

        if (!isCameraInitialized) {
            camera.position.set(targetX, targetY, 0);
            isCameraInitialized = true;
        } else {
            float lerpSpeed = 5.0f;
            camera.position.x += (targetX - camera.position.x) * lerpSpeed * Gdx.graphics.getDeltaTime();
            camera.position.y += (targetY - camera.position.y) * lerpSpeed * Gdx.graphics.getDeltaTime();
        }

        if (world.cameraShakeTimer > 0) {
            camera.position.x += MathUtils.random(-world.cameraShakeIntensity, world.cameraShakeIntensity);
            camera.position.y += MathUtils.random(-world.cameraShakeIntensity, world.cameraShakeIntensity);
        }
    }

    // --- Environment & Effects Rendering ---
    private void renderCrossroadsDust(float delta) {
        if (crossroadsDustEffect == null)
            return;

        boolean isInForgottenCrossroads = world.currentRegion == GameRegion.FORGOTTEN_CROSSROADS;
        if (!isInForgottenCrossroads) {
            if (crossroadsDustWasActive)
                crossroadsDustEffect.reset();
            crossroadsDustWasActive = false;
            return;
        }

        crossroadsDustWasActive = true;

        Rectangle playerBounds = world.player.getBounds();
        float playerCenterX = playerBounds.x + playerBounds.width / 2f;
        float playerCenterY = playerBounds.y + playerBounds.height / 2f;

        boolean playerDashing = world.player.animation != null && world.player.animation.name().contains("DASH");
        boolean playerFacingRight = world.player.getDirection() == Constants.RIGHT_DIRECTION;

        crossroadsDustEffect.update(delta, camera, playerCenterX, playerCenterY, playerDashing, playerFacingRight);

        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        crossroadsDustEffect.draw(batch);
        batch.end();
    }

    private void updateGreenpathAmbient(float delta) {
        if (greenpathAmbientEffect == null)
            return;

        boolean isInGreenpath = isCurrentRegionNamed("GREENPATH");
        if (!isInGreenpath) {
            if (greenpathEffectWasActive)
                greenpathAmbientEffect.reset();
            greenpathEffectWasActive = false;
            return;
        }

        greenpathEffectWasActive = true;

        Rectangle playerBounds = world.player.getBounds();
        float playerCenterX = playerBounds.x + playerBounds.width / 2f;
        float playerCenterY = playerBounds.y + playerBounds.height / 2f;

        boolean playerDashing = world.player.animation != null && world.player.animation.name().contains("DASH");
        boolean playerFacingRight = world.player.getDirection() == Constants.RIGHT_DIRECTION;

        greenpathAmbientEffect.update(delta, camera, playerCenterX, playerCenterY, playerDashing, playerFacingRight);
    }

    private void renderGreenpathBackground() {
        if (!greenpathEffectWasActive || greenpathAmbientEffect == null)
            return;

        batch.enableBlending();
        batch.begin();
        greenpathAmbientEffect.drawBackground(batch);
        batch.end();
    }

    private void renderGreenpathForeground() {
        if (!greenpathEffectWasActive || greenpathAmbientEffect == null)
            return;

        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending();
        batch.begin();
        greenpathAmbientEffect.drawForeground(batch);
        batch.end();
    }

    private boolean isCurrentRegionNamed(String expectedName) {
        if (world.currentRegion == null || expectedName == null)
            return false;

        String normalizedCurrentName = world.currentRegion.name().replace("_", "").replace("-", "").replace(" ", "");
        String normalizedExpectedName = expectedName.replace("_", "").replace("-", "").replace(" ", "");

        return normalizedExpectedName.equalsIgnoreCase(normalizedCurrentName);
    }

    // --- Entity Rendering ---
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
            float baseOffsetX = (spriteWidth - Constants.PLAYER_HITBOX_WIDTH) / 2f;

            float x = world.player.position.x - baseOffsetX + (effect.type.xOffset * effect.direction);
            float y = world.player.position.y + effect.type.yOffset;
            float scaleX = (effect.type == PlayerEffectAnimation.BLAST) ? -effect.direction : effect.direction;

            batch.draw(keyFrame, x, y, spriteWidth / 2f, spriteHeight / 2f, spriteWidth, spriteHeight,
                    scaleX * effect.type.sclaeX, 1, 0);
        }
    }

    private void renderProjectiles(SpriteBatch batch) {
        for (PlayerProjectile proj : world.projectiles) {
            PlayerEffectAnimation effectType = world.player.hasCharm(CharmType.VOID_HEART)
                    ? (proj.isExploding ? PlayerEffectAnimation.SHADOW_BALL_END : PlayerEffectAnimation.SHADOW_BALL)
                    : (proj.isExploding ? PlayerEffectAnimation.SOUL_BALL_END : PlayerEffectAnimation.SOUL_BALL);

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

    private void renderZote(SpriteBatch batch) {
        if (world.zote == null)
            return;

        Animation<TextureRegion> animation = GameAssetManager.zoteAnimationMap.get(world.zote.animation);
        TextureRegion frame = animation.getKeyFrame(world.zote.animationTime);

        boolean shouldFaceLeft = world.zote.isFacingRight;
        if (frame.isFlipX() != shouldFaceLeft) {
            frame.flip(true, false);
        }

        batch.draw(frame, world.zote.position.x - frame.getRegionWidth() / 2f, world.zote.position.y);
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
                font.draw(batch, "Press E to interact", 0,
                        Gdx.graphics.getHeight() / 2f - 300f, Gdx.graphics.getWidth(), Align.center, false);
            }
        }
        batch.end();
    }

    private void renderBossFightEffects(SpriteBatch batch) {
        Rectangle bossDoor = world.bossDoor;
        float animatedY = bossDoor.y + (bossDoor.height * (1f - world.gateDropProgress));

        batch.draw(GameAssetManager.gateTexture, bossDoor.x, animatedY, bossDoor.width, bossDoor.height);
    }

    private void renderShockwaves(SpriteBatch batch) {
        for (Enemy enemy : world.enemies) {
            if (enemy instanceof FalseKnight) {
                FalseKnight fk = (FalseKnight) enemy;

                for (FalseKnight.Shockwave wave : fk.shockwaves) {
                    Animation<TextureRegion> animation = GameAssetManager.enemyAnimationMap
                            .get(EnemyAnimations.FALSE_KNIGHT_SHOCKWAVE);
                    TextureRegion frame = animation.getKeyFrame(wave.lifetime);

                    float spriteWidth = frame.getRegionWidth();
                    float spriteHeight = frame.getRegionHeight();
                    float x = wave.velocityX < 0f ? wave.bounds.x - 10
                            : wave.bounds.x + wave.bounds.width - spriteWidth + 10;

                    batch.draw(frame, x, wave.bounds.y + spriteHeight / 2 - 15,
                            spriteWidth / 2f, spriteHeight / 2f,
                            spriteWidth, spriteHeight,
                            wave.getDir(), 2, 0);
                }
            }
        }
    }

    // --- Debug Rendering ---
    private void renderDebugHitboxes() {
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

                Rectangle mace = fk.getActiveAttackHitbox();
                if (mace != null) {
                    shapeRenderer.setColor(Color.MAGENTA);
                    shapeRenderer.rect(mace.x, mace.y, mace.width, mace.height);
                }

                shapeRenderer.setColor(Color.YELLOW);
                for (FalseKnight.Shockwave wave : fk.shockwaves) {
                    shapeRenderer.rect(wave.bounds.x, wave.bounds.y, wave.bounds.width, wave.bounds.height);
                }

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
}
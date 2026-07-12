package com.hollowknight.views;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hollowknight.models.player.HealthMask;
import com.hollowknight.models.player.PlayerVitals;
import com.hollowknight.models.world.GameWorld;

import java.util.List;

public class HUDRenderer {

    // --- Core Systems ---
    private final GameWorld world;
    private final SpriteBatch batch;

    // --- Layout Configuration ---
    private static final float START_OFFSET_X = 140f;
    private static final float START_OFFSET_Y = 145f;
    private static final float SPACING = 70f;
    private static final float MASK_SIZE = 100f;

    // --- Initialization ---
    public HUDRenderer(GameWorld world) {
        this.world = world;
        this.batch = new SpriteBatch();
    }

    // --- Core Render Loop ---
    public void render(OrthographicCamera camera) {
        // Sync the SpriteBatch with the HUD camera's projection matrix
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        PlayerVitals vitals = world.player.getVitals();

        // 1. Calculate base layout positions
        float masksStartX = camera.position.x - (camera.viewportWidth / 2f) + START_OFFSET_X;
        float masksStartY = camera.position.y + (camera.viewportHeight / 2f) - START_OFFSET_Y;

        float barStartX = masksStartX - 320;
        float barStartY = masksStartY - 60;

        float soulPercent = Math.max(0, Math.min(vitals.getSoulsInAnimation() / 99f, 1f));

        // 2. Draw the background frame
        batch.draw(GameAssetManager.healthBar, barStartX, barStartY);

        // 3. Draw the Soul Orb
        if (soulPercent > 0) {
            int index = (int) (soulPercent * (GameAssetManager.soulsTextures.length - 1));
            Texture soulsTexture = GameAssetManager.soulsTextures[index];

            float fullWidth = soulsTexture.getWidth();
            float fullHeight = soulsTexture.getHeight();

            float orbOffsetX = 250f - 66 + 3;
            float orbOffsetY = 0 - 14 + 3;

            float drawX = barStartX + orbOffsetX;
            float drawY = barStartY + orbOffsetY;

            batch.draw(soulsTexture, drawX, drawY, fullWidth * 2.9f, fullHeight * 2.9f);
        }

        // 4. Draw the Health Masks
        List<HealthMask> masks = vitals.getHealthMasks();

        for (int i = 0; i < masks.size(); i++) {
            HealthMask mask = masks.get(i);
            Animation<TextureRegion> animation = GameAssetManager.healthAnimationMap.get(mask.state);

            if (animation != null) {
                TextureRegion currentFrame = animation.getKeyFrame(mask.stateTime);
                float drawX = masksStartX + (i * SPACING);
                batch.draw(currentFrame, drawX, masksStartY, MASK_SIZE, MASK_SIZE);
            }
        }

        batch.end();
    }

    // --- Cleanup ---
    public void dispose() {
        batch.dispose();
    }
}
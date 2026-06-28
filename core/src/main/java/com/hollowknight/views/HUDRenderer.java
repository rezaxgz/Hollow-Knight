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

    private final GameWorld world;
    private final SpriteBatch batch;

    // Layout configuration for the health masks
    private static final float START_OFFSET_X = 140f;
    private static final float START_OFFSET_Y = 145f; // Distance down from the top edge
    private static final float SPACING = 70f; // Horizontal space between masks
    private static final float MASK_SIZE = 100f; // Width & height to draw the masks

    public HUDRenderer(GameWorld world) {
        this.world = world;
        this.batch = new SpriteBatch();
    }

    public void render(OrthographicCamera camera) {
        // Sync the SpriteBatch with the camera's view
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        PlayerVitals vitals = world.player.getVitals();

        float masksStartX = camera.position.x - (camera.viewportWidth / 2f) + START_OFFSET_X;
        float masksStartY = camera.position.y + (camera.viewportHeight / 2f) - START_OFFSET_Y;

        float barStartX = masksStartX - 320;
        float barStartY = masksStartY - 60;

        float soulPercent = Math.max(0, Math.min(vitals.getSoulsInAnimation() / 99f, 1f)); // Clamped between 0 and 1

        // 2. Draw the background frame first
        batch.draw(GameAssetManager.healthBar, barStartX, barStartY);

        // If there are no souls, we can skip drawing the circle entirely
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

        List<HealthMask> masks = vitals.getHealthMasks();

        // Calculate the top-left starting position based on the camera

        for (int i = 0; i < masks.size(); i++) {
            HealthMask mask = masks.get(i);

            // Fetch the animation for the current state (FULL, BREAKING, EMPTY, HEALING)
            Animation<TextureRegion> animation = GameAssetManager.healthAnimationMap.get(mask.state);

            if (animation != null) {
                // Determine the correct frame using the elapsed stateTime
                TextureRegion currentFrame = animation.getKeyFrame(mask.stateTime);

                // Calculate the X position for this specific slot
                float drawX = masksStartX + (i * SPACING);

                // Render the frame
                batch.draw(currentFrame, drawX, masksStartY, MASK_SIZE, MASK_SIZE);
            }
        }

        batch.end();
    }

    public void dispose() {
        batch.dispose();
    }
}
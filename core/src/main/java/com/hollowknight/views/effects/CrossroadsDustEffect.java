package com.hollowknight.views.effects;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Subtle, camera-aware dust motes for Forgotten Crossroads.
 *
 * The texture is generated at runtime so the effect does not require an
 * external PNG. Particles live in world space and are recycled around the
 * visible camera area, which keeps the density stable throughout the room.
 */
public final class CrossroadsDustEffect {

    private static final int BACKGROUND_COUNT = 72;
    private static final int MIDGROUND_COUNT = 54;
    private static final int FOREGROUND_COUNT = 28;
    private static final int PARTICLE_COUNT = BACKGROUND_COUNT + MIDGROUND_COUNT + FOREGROUND_COUNT;

    private static final float SPAWN_PADDING = 130f;
    private static final float CAMERA_RESET_DISTANCE = 1500f;
    private static final float DASH_PUSH_RADIUS = 190f;
    private static final float MAX_UPDATE_DELTA = 1f / 20f;

    private final Texture dustTexture;
    private final DustParticle[] particles = new DustParticle[PARTICLE_COUNT];

    private boolean initialized;
    private float lastCameraX;
    private float lastCameraY;
    private float effectTime;

    public CrossroadsDustEffect() {
        dustTexture = createSoftDustTexture();
        dustTexture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);

        for (int i = 0; i < particles.length; i++) {
            int layer;

            if (i < BACKGROUND_COUNT) {
                layer = 0;
            } else if (i < BACKGROUND_COUNT + MIDGROUND_COUNT) {
                layer = 1;
            } else {
                layer = 2;
            }

            particles[i] = new DustParticle(layer);
        }
    }

    public void update(
            float delta,
            OrthographicCamera camera,
            float playerX,
            float playerY,
            boolean playerDashing,
            boolean playerFacingRight) {
        if (camera == null) {
            return;
        }

        float safeDelta = MathUtils.clamp(
                delta,
                0f,
                MAX_UPDATE_DELTA);

        float halfWidth = camera.viewportWidth * camera.zoom / 2f;
        float halfHeight = camera.viewportHeight * camera.zoom / 2f;

        float left = camera.position.x - halfWidth - SPAWN_PADDING;
        float right = camera.position.x + halfWidth + SPAWN_PADDING;
        float bottom = camera.position.y - halfHeight - SPAWN_PADDING;
        float top = camera.position.y + halfHeight + SPAWN_PADDING;

        if (!initialized
                || VectorDistance.isFartherThan(
                        camera.position.x,
                        camera.position.y,
                        lastCameraX,
                        lastCameraY,
                        CAMERA_RESET_DISTANCE)) {
            populateVisibleArea(left, right, bottom, top);
            initialized = true;
        }

        lastCameraX = camera.position.x;
        lastCameraY = camera.position.y;
        effectTime += safeDelta;

        float dashDirection = playerFacingRight ? 1f : -1f;
        float dashRadiusSquared = DASH_PUSH_RADIUS * DASH_PUSH_RADIUS;

        for (DustParticle particle : particles) {
            float sway = MathUtils.sin(
                    effectTime * particle.swaySpeed
                            + particle.phase)
                    * particle.swayAmount;

            particle.x += (particle.horizontalSpeed + sway)
                    * safeDelta;
            particle.y -= particle.fallSpeed * safeDelta;
            particle.rotation += particle.rotationSpeed * safeDelta;

            if (playerDashing && safeDelta > 0f) {
                applyDashWake(
                        particle,
                        playerX,
                        playerY,
                        dashDirection,
                        dashRadiusSquared,
                        safeDelta);
            }

            recycleIfOutside(
                    particle,
                    left,
                    right,
                    bottom,
                    top);
        }
    }

    public void draw(SpriteBatch batch) {
        if (!initialized || batch == null) {
            return;
        }

        for (DustParticle particle : particles) {
            float pulse = 0.90f
                    + 0.10f * MathUtils.sin(
                            effectTime * particle.pulseSpeed
                                    + particle.phase);

            float alpha = MathUtils.clamp(
                    particle.baseAlpha * pulse * 1.25f,
                    0f,
                    0.82f);

            batch.setColor(
                    particle.red,
                    particle.green,
                    particle.blue,
                    alpha);

            batch.draw(
                    dustTexture,
                    particle.x - particle.width / 2f,
                    particle.y - particle.height / 2f,
                    particle.width / 2f,
                    particle.height / 2f,
                    particle.width,
                    particle.height,
                    1f,
                    1f,
                    particle.rotation,
                    0,
                    0,
                    dustTexture.getWidth(),
                    dustTexture.getHeight(),
                    false,
                    false);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void reset() {
        initialized = false;
    }

    public void dispose() {
        dustTexture.dispose();
    }

    private void populateVisibleArea(
            float left,
            float right,
            float bottom,
            float top) {
        for (DustParticle particle : particles) {
            particle.randomizeAppearance();
            particle.x = MathUtils.random(left, right);
            particle.y = MathUtils.random(bottom, top);
        }
    }

    private void recycleIfOutside(
            DustParticle particle,
            float left,
            float right,
            float bottom,
            float top) {
        boolean recycled = false;

        if (particle.y < bottom) {
            particle.y = top;
            particle.x = MathUtils.random(left, right);
            recycled = true;
        } else if (particle.y > top) {
            particle.y = bottom;
            particle.x = MathUtils.random(left, right);
            recycled = true;
        }

        if (particle.x < left) {
            particle.x = right;
            particle.y = MathUtils.random(bottom, top);
            recycled = true;
        } else if (particle.x > right) {
            particle.x = left;
            particle.y = MathUtils.random(bottom, top);
            recycled = true;
        }

        if (recycled) {
            particle.randomizeAppearance();
        }
    }

    private void applyDashWake(
            DustParticle particle,
            float playerX,
            float playerY,
            float dashDirection,
            float dashRadiusSquared,
            float delta) {
        float dx = particle.x - playerX;
        float dy = particle.y - playerY;
        float distanceSquared = dx * dx + dy * dy;

        if (distanceSquared <= 0.001f
                || distanceSquared >= dashRadiusSquared) {
            return;
        }

        float distance = (float) Math.sqrt(distanceSquared);
        float strength = 1f - distance / DASH_PUSH_RADIUS;
        float layerStrength = 0.72f + particle.layer * 0.18f;

        float outwardX = dx / distance;
        float outwardY = dy / distance;

        particle.x += (outwardX * 90f
                + dashDirection * 150f) * strength * layerStrength * delta;

        particle.y += outwardY
                * 75f
                * strength
                * layerStrength
                * delta;
    }

    private static Texture createSoftDustTexture() {
        final int size = 18;
        final float center = (size - 1) / 2f;
        final float radius = size / 2f;

        Pixmap pixmap = new Pixmap(
                size,
                size,
                Pixmap.Format.RGBA8888);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float normalizedDistance = (float) Math.sqrt(dx * dx + dy * dy)
                        / radius;

                float alpha = MathUtils.clamp(
                        1f - normalizedDistance,
                        0f,
                        1f);

                alpha = alpha * alpha;

                pixmap.setColor(1f, 1f, 1f, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static final class DustParticle {

        private final int layer;

        private float x;
        private float y;
        private float width;
        private float height;
        private float horizontalSpeed;
        private float fallSpeed;
        private float swayAmount;
        private float swaySpeed;
        private float rotation;
        private float rotationSpeed;
        private float baseAlpha;
        private float pulseSpeed;
        private float phase;
        private float red;
        private float green;
        private float blue;

        private DustParticle(int layer) {
            this.layer = layer;
            randomizeAppearance();
        }

        private void randomizeAppearance() {
            phase = MathUtils.random(0f, MathUtils.PI2);
            rotation = MathUtils.random(0f, 360f);

            if (layer == 0) {
                width = MathUtils.random(5.5f, 9f);
                height = width * MathUtils.random(0.65f, 1.35f);
                horizontalSpeed = MathUtils.random(-5f, 5f);
                fallSpeed = MathUtils.random(4f, 10f);
                swayAmount = MathUtils.random(2f, 5f);
                swaySpeed = MathUtils.random(0.35f, 0.75f);
                rotationSpeed = MathUtils.random(-7f, 7f);
                baseAlpha = MathUtils.random(0.25f, 0.40f);
                pulseSpeed = MathUtils.random(0.45f, 0.85f);
                red = 0.76f;
                green = 0.84f;
                blue = 0.90f;
            } else if (layer == 1) {
                width = MathUtils.random(9f, 15f);
                height = width * MathUtils.random(0.55f, 1.30f);
                horizontalSpeed = MathUtils.random(-8f, 8f);
                fallSpeed = MathUtils.random(8f, 17f);
                swayAmount = MathUtils.random(4f, 9f);
                swaySpeed = MathUtils.random(0.55f, 1.05f);
                rotationSpeed = MathUtils.random(-12f, 12f);
                baseAlpha = MathUtils.random(0.32f, 0.52f);
                pulseSpeed = MathUtils.random(0.65f, 1.20f);
                red = 0.82f;
                green = 0.89f;
                blue = 0.95f;
            } else {
                width = MathUtils.random(14f, 23f);
                height = width * MathUtils.random(0.50f, 1.20f);
                horizontalSpeed = MathUtils.random(-12f, 12f);
                fallSpeed = MathUtils.random(12f, 25f);
                swayAmount = MathUtils.random(7f, 14f);
                swaySpeed = MathUtils.random(0.75f, 1.35f);
                rotationSpeed = MathUtils.random(-18f, 18f);
                baseAlpha = MathUtils.random(0.28f, 0.48f);
                pulseSpeed = MathUtils.random(0.80f, 1.45f);
                red = 0.88f;
                green = 0.94f;
                blue = 1.00f;
            }
        }
    }

    private static final class VectorDistance {

        private VectorDistance() {
        }

        private static boolean isFartherThan(
                float x1,
                float y1,
                float x2,
                float y2,
                float distance) {
            float dx = x1 - x2;
            float dy = y1 - y2;
            return dx * dx + dy * dy > distance * distance;
        }
    }
}
package com.hollowknight.views.effects;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * A layered Greenpath ambience made from glowing pollen, plant spores, and
 * drifting leaf fragments. Both particle textures are generated at runtime,
 * so this effect does not require additional image assets.
 */
public final class GreenpathAmbientEffect {

    private static final int POLLEN_COUNT = 68;
    private static final int SPORE_COUNT = 38;
    private static final int LEAF_COUNT = 24;
    private static final int PARTICLE_COUNT = POLLEN_COUNT + SPORE_COUNT + LEAF_COUNT;

    private static final int TYPE_POLLEN = 0;
    private static final int TYPE_SPORE = 1;
    private static final int TYPE_LEAF = 2;

    private static final float SPAWN_PADDING = 150f;
    private static final float CAMERA_RESET_DISTANCE = 1500f;
    private static final float DASH_PUSH_RADIUS = 210f;
    private static final float MAX_UPDATE_DELTA = 1f / 20f;

    private final Texture glowTexture;
    private final Texture leafTexture;
    private final PlantParticle[] particles = new PlantParticle[PARTICLE_COUNT];

    private boolean initialized;
    private float lastCameraX;
    private float lastCameraY;
    private float effectTime;

    public GreenpathAmbientEffect() {
        glowTexture = createSoftGlowTexture();
        leafTexture = createLeafTexture();

        glowTexture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
        leafTexture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);

        for (int i = 0; i < particles.length; i++) {
            int type;
            if (i < POLLEN_COUNT) {
                type = TYPE_POLLEN;
            } else if (i < POLLEN_COUNT + SPORE_COUNT) {
                type = TYPE_SPORE;
            } else {
                type = TYPE_LEAF;
            }
            particles[i] = new PlantParticle(type);
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

        float safeDelta = MathUtils.clamp(delta, 0f, MAX_UPDATE_DELTA);
        float halfWidth = camera.viewportWidth * camera.zoom / 2f;
        float halfHeight = camera.viewportHeight * camera.zoom / 2f;

        float left = camera.position.x - halfWidth - SPAWN_PADDING;
        float right = camera.position.x + halfWidth + SPAWN_PADDING;
        float bottom = camera.position.y - halfHeight - SPAWN_PADDING;
        float top = camera.position.y + halfHeight + SPAWN_PADDING;

        if (!initialized || isCameraFarFromLastPosition(camera)) {
            populateVisibleArea(left, right, bottom, top);
            initialized = true;
        }

        lastCameraX = camera.position.x;
        lastCameraY = camera.position.y;
        effectTime += safeDelta;

        float dashDirection = playerFacingRight ? 1f : -1f;
        float dashRadiusSquared = DASH_PUSH_RADIUS * DASH_PUSH_RADIUS;

        for (PlantParticle particle : particles) {
            updateParticle(particle, safeDelta);

            if (playerDashing && safeDelta > 0f) {
                applyDashWake(
                        particle,
                        playerX,
                        playerY,
                        dashDirection,
                        dashRadiusSquared,
                        safeDelta);
            }

            recycleIfOutside(particle, left, right, bottom, top);
        }
    }

    /** Draws pollen and spores behind gameplay actors. */
    public void drawBackground(SpriteBatch batch) {
        if (!initialized || batch == null) {
            return;
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (PlantParticle particle : particles) {
            if (particle.type == TYPE_LEAF) {
                continue;
            }
            drawGlowParticle(batch, particle);
        }
        restoreBatch(batch);
    }

    /** Draws larger leaf fragments in front of the map foreground. */
    public void drawForeground(SpriteBatch batch) {
        if (!initialized || batch == null) {
            return;
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        for (PlantParticle particle : particles) {
            if (particle.type != TYPE_LEAF) {
                continue;
            }
            drawLeafParticle(batch, particle);
        }
        restoreBatch(batch);
    }

    public void reset() {
        initialized = false;
    }

    public void dispose() {
        glowTexture.dispose();
        leafTexture.dispose();
    }

    private void updateParticle(PlantParticle particle, float delta) {
        float wind = MathUtils.sin(effectTime * 0.32f) * 6f;
        float sway = MathUtils.sin(
                effectTime * particle.swaySpeed + particle.phase)
                * particle.swayAmount;

        if (particle.type == TYPE_LEAF) {
            particle.x += (particle.horizontalSpeed + wind + sway) * delta;
            particle.y -= particle.verticalSpeed * delta;
            particle.rotation += particle.rotationSpeed * delta;
            return;
        }

        particle.x += (particle.horizontalSpeed + wind * 0.35f + sway) * delta;
        particle.y += particle.verticalSpeed * delta;
        particle.rotation += particle.rotationSpeed * delta;
    }

    private void drawGlowParticle(SpriteBatch batch, PlantParticle particle) {
        float pulse = 0.82f
                + 0.18f * MathUtils.sin(
                        effectTime * particle.pulseSpeed + particle.phase);
        float alpha = MathUtils.clamp(particle.baseAlpha * pulse, 0f, 0.72f);

        batch.setColor(particle.red, particle.green, particle.blue, alpha);
        batch.draw(
                glowTexture,
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
                glowTexture.getWidth(),
                glowTexture.getHeight(),
                false,
                false);
    }

    private void drawLeafParticle(SpriteBatch batch, PlantParticle particle) {
        float flutter = 0.86f
                + 0.14f * MathUtils.sin(
                        effectTime * particle.pulseSpeed + particle.phase);
        float alpha = MathUtils.clamp(particle.baseAlpha * flutter, 0f, 0.82f);

        batch.setColor(particle.red, particle.green, particle.blue, alpha);
        batch.draw(
                leafTexture,
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
                leafTexture.getWidth(),
                leafTexture.getHeight(),
                false,
                particle.flipY);
    }

    private void restoreBatch(SpriteBatch batch) {
        batch.setColor(1f, 1f, 1f, 1f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void populateVisibleArea(
            float left,
            float right,
            float bottom,
            float top) {
        for (PlantParticle particle : particles) {
            particle.randomizeAppearance();
            particle.x = MathUtils.random(left, right);
            particle.y = MathUtils.random(bottom, top);
        }
    }

    private void recycleIfOutside(
            PlantParticle particle,
            float left,
            float right,
            float bottom,
            float top) {
        boolean recycled = false;

        if (particle.type == TYPE_LEAF) {
            if (particle.y < bottom) {
                particle.y = top;
                particle.x = MathUtils.random(left, right);
                recycled = true;
            } else if (particle.y > top) {
                particle.y = bottom;
                particle.x = MathUtils.random(left, right);
                recycled = true;
            }
        } else {
            if (particle.y > top) {
                particle.y = bottom;
                particle.x = MathUtils.random(left, right);
                recycled = true;
            } else if (particle.y < bottom) {
                particle.y = top;
                particle.x = MathUtils.random(left, right);
                recycled = true;
            }
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
            PlantParticle particle,
            float playerX,
            float playerY,
            float dashDirection,
            float dashRadiusSquared,
            float delta) {
        float dx = particle.x - playerX;
        float dy = particle.y - playerY;
        float distanceSquared = dx * dx + dy * dy;

        if (distanceSquared <= 0.001f || distanceSquared >= dashRadiusSquared) {
            return;
        }

        float distance = (float) Math.sqrt(distanceSquared);
        float strength = 1f - distance / DASH_PUSH_RADIUS;
        float typeStrength = particle.type == TYPE_LEAF ? 1.15f : 0.78f;
        float outwardX = dx / distance;
        float outwardY = dy / distance;

        particle.x += (outwardX * 105f + dashDirection * 185f)
                * strength
                * typeStrength
                * delta;
        particle.y += outwardY
                * 92f
                * strength
                * typeStrength
                * delta;
        particle.rotation += dashDirection * 120f * strength * delta;
    }

    private boolean isCameraFarFromLastPosition(OrthographicCamera camera) {
        float dx = camera.position.x - lastCameraX;
        float dy = camera.position.y - lastCameraY;
        return dx * dx + dy * dy > CAMERA_RESET_DISTANCE * CAMERA_RESET_DISTANCE;
    }

    private static Texture createSoftGlowTexture() {
        final int size = 24;
        final float center = (size - 1) / 2f;
        final float radius = size / 2f;

        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float normalizedDistance = (float) Math.sqrt(dx * dx + dy * dy) / radius;
                float alpha = MathUtils.clamp(1f - normalizedDistance, 0f, 1f);
                alpha = alpha * alpha;
                pixmap.setColor(1f, 1f, 1f, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static Texture createLeafTexture() {
        final int width = 28;
        final int height = 16;
        final float centerX = (width - 1) / 2f;
        final float centerY = (height - 1) / 2f;
        final float halfWidth = width / 2f;
        final float halfHeight = height / 2f;

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float normalizedX = (x - centerX) / halfWidth;
                float normalizedY = (y - centerY) / halfHeight;
                float taper = MathUtils.clamp(1f - Math.abs(normalizedX), 0f, 1f);
                float allowedHalfHeight = (float) Math.pow(taper, 0.55f);

                if (allowedHalfHeight <= 0f || Math.abs(normalizedY) > allowedHalfHeight) {
                    continue;
                }

                float edgeDistance = Math.abs(normalizedY) / allowedHalfHeight;
                float alpha = MathUtils.clamp((1f - edgeDistance) * 1.8f, 0f, 1f);
                alpha *= MathUtils.clamp(taper * 2.4f, 0f, 1f);

                pixmap.setColor(1f, 1f, 1f, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static final class PlantParticle {

        private final int type;

        private float x;
        private float y;
        private float width;
        private float height;
        private float horizontalSpeed;
        private float verticalSpeed;
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
        private boolean flipY;

        private PlantParticle(int type) {
            this.type = type;
            randomizeAppearance();
        }

        private void randomizeAppearance() {
            phase = MathUtils.random(0f, MathUtils.PI2);
            rotation = MathUtils.random(0f, 360f);
            flipY = MathUtils.randomBoolean();

            if (type == TYPE_POLLEN) {
                width = MathUtils.random(4f, 8f);
                height = width;
                horizontalSpeed = MathUtils.random(-5f, 5f);
                verticalSpeed = MathUtils.random(5f, 13f);
                swayAmount = MathUtils.random(2f, 6f);
                swaySpeed = MathUtils.random(0.55f, 1.15f);
                rotationSpeed = MathUtils.random(-8f, 8f);
                baseAlpha = MathUtils.random(0.24f, 0.44f);
                pulseSpeed = MathUtils.random(0.8f, 1.8f);
                red = MathUtils.random(0.55f, 0.72f);
                green = MathUtils.random(0.90f, 1.00f);
                blue = MathUtils.random(0.38f, 0.55f);
            } else if (type == TYPE_SPORE) {
                width = MathUtils.random(10f, 19f);
                height = width * MathUtils.random(0.82f, 1.18f);
                horizontalSpeed = MathUtils.random(-8f, 8f);
                verticalSpeed = MathUtils.random(3f, 9f);
                swayAmount = MathUtils.random(5f, 12f);
                swaySpeed = MathUtils.random(0.35f, 0.85f);
                rotationSpeed = MathUtils.random(-12f, 12f);
                baseAlpha = MathUtils.random(0.18f, 0.34f);
                pulseSpeed = MathUtils.random(0.45f, 1.05f);
                red = MathUtils.random(0.20f, 0.38f);
                green = MathUtils.random(0.76f, 0.94f);
                blue = MathUtils.random(0.40f, 0.62f);
            } else {
                width = MathUtils.random(18f, 34f);
                height = width * MathUtils.random(0.42f, 0.62f);
                horizontalSpeed = MathUtils.random(-13f, 13f);
                verticalSpeed = MathUtils.random(12f, 28f);
                swayAmount = MathUtils.random(8f, 18f);
                swaySpeed = MathUtils.random(0.65f, 1.35f);
                rotationSpeed = MathUtils.random(-42f, 42f);
                baseAlpha = MathUtils.random(0.30f, 0.58f);
                pulseSpeed = MathUtils.random(0.70f, 1.40f);
                red = MathUtils.random(0.18f, 0.36f);
                green = MathUtils.random(0.56f, 0.84f);
                blue = MathUtils.random(0.18f, 0.34f);
            }
        }
    }
}
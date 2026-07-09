package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.hollowknight.controller.GameController;
import com.hollowknight.models.achievements.Achievement;
import com.hollowknight.models.achievements.AchievementManager;
import com.hollowknight.models.achievements.AchievementObserver;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.GameRenderer;
import com.hollowknight.views.actors.modals.AchievementPopup;

public class GameScreen extends AbstractScreen implements AchievementObserver {
    GameWorld world;
    private GameController controller;
    private GameRenderer renderer;

    private FrameBuffer blurFbo;
    private SpriteBatch fboBatch;

    public GameScreen(GameWorld world) {
        this.world = world;
        controller = GameController.init(world);
        renderer = new GameRenderer(world);
        fboBatch = new SpriteBatch();
    }

    @Override
    public void show() {
        super.show(); // Sets input to AbstractScreen's stage
        renderer.show(); // Overwrites input with GameRenderer's multiplexer
        GameAssetManager.menuBgm.stop();

        AchievementManager.getInstance().setObserver(this);

        InputProcessor currentProcessor = Gdx.input.getInputProcessor();
        if (currentProcessor instanceof InputMultiplexer) {
            InputMultiplexer multiplexer = (InputMultiplexer) currentProcessor;
            // Add at index 0 so the pause menu UI intercepts clicks before the game world
            // does
            multiplexer.addProcessor(0, this.stage);
        }

    }

    @Override
    public void render(float delta) {
        float cappedDelta = Math.min(delta, 1 / 30f);
        controller.update(cappedDelta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean isPaused = GameController.getInstance().isPaused;

        if (isPaused) {
            Gdx.graphics.setCursor(GameAssetManager.customCursor);
        } else {
            Gdx.graphics.setCursor(GameAssetManager.blankCursor);
        }

        if (isPaused) {
            // 1. Initialize FBO at 1/4 resolution for the blur effect
            if (blurFbo == null) {
                blurFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth() / 4,
                        Gdx.graphics.getHeight() / 4, false);
                blurFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }

            // 2. Render the crisp world into the tiny FBO
            blurFbo.begin();
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            renderer.renderWorld();
            blurFbo.end();

            // 3. Draw the tiny FBO upscaled to the full screen (Creates the blur)
            fboBatch.begin();
            // Y is inverted during FBO extraction
            fboBatch.draw(blurFbo.getColorBufferTexture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(),
                    -Gdx.graphics.getHeight());

            // Optional: Draw a dark semi-transparent tint over the blur for better UI
            // readability
            fboBatch.setColor(0, 0, 0, 0.4f);
            fboBatch.draw(GameAssetManager.pixelTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            fboBatch.setColor(1, 1, 1, 1);
            fboBatch.end();

        } else {
            // Normal unpaused rendering
            renderer.renderWorld();
        }

        // UI is always rendered at full resolution on top
        renderer.renderUI(cappedDelta);
        // Render global UI components (This is what draws the PauseModal!)
        super.render(cappedDelta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        renderer.resize(width, height);

        // Trash the old buffer so it scales correctly if window is resized
        if (blurFbo != null) {
            blurFbo.dispose();
            blurFbo = null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (blurFbo != null)
            blurFbo.dispose();
        fboBatch.dispose();
    }

    @Override
    public void onAchievementUnlocked(Achievement achievement) {
        // Renders the popup on the AbstractScreen's stage
        AchievementPopup.show(this.stage, achievement);
    }

    @Override
    public void hide() {
        super.hide();
        // Prevent memory leaks when switching screens
        AchievementManager.getInstance().setObserver(null);
    }
}
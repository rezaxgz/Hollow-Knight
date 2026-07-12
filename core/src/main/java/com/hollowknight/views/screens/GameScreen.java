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

    // --- Game Systems ---
    GameWorld world;
    private GameController controller;
    private GameRenderer renderer;

    // --- Rendering Resources ---
    private FrameBuffer blurFbo;
    private SpriteBatch fboBatch;
    private boolean wasPaused = false;

    // --- Initialization & Lifecycle ---
    public GameScreen(GameWorld world) {
        this.world = world;
        controller = GameController.init(world);
        renderer = new GameRenderer(world);
        fboBatch = new SpriteBatch();
    }

    @Override
    public void show() {
        super.show();
        renderer.show();
        GameAssetManager.menuBgm.stop();

        AchievementManager.getInstance().setObserver(this);

        InputProcessor currentProcessor = Gdx.input.getInputProcessor();
        if (currentProcessor instanceof InputMultiplexer) {
            InputMultiplexer multiplexer = (InputMultiplexer) currentProcessor;
            multiplexer.addProcessor(0, this.stage);
        }

        Gdx.graphics.setCursor(GameAssetManager.blankCursor);
        Gdx.input.setCursorCatched(true);
        GameController.getInstance().isPaused = false;
    }

    @Override
    public void hide() {
        super.hide();
        AchievementManager.getInstance().setObserver(null);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        renderer.resize(width, height);

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
        renderer.dispose();
    }

    // --- Core Render Loop ---
    @Override
    public void render(float delta) {
        float cappedDelta = Math.min(delta, 1 / 30f);
        controller.update(cappedDelta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean isPaused = GameController.getInstance().isPaused;

        if (isPaused != wasPaused) {
            if (isPaused) {
                Gdx.graphics.setCursor(GameAssetManager.customCursor);
                Gdx.input.setCursorCatched(false);
            } else {
                Gdx.graphics.setCursor(GameAssetManager.blankCursor);
                Gdx.input.setCursorCatched(true);
            }
            wasPaused = isPaused;
        }

        if (isPaused) {
            if (blurFbo == null) {
                blurFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth() / 4,
                        Gdx.graphics.getHeight() / 4, false);
                blurFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }

            blurFbo.begin();
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            renderer.renderWorld(0f);
            blurFbo.end();

            fboBatch.begin();
            fboBatch.draw(blurFbo.getColorBufferTexture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(),
                    -Gdx.graphics.getHeight());

            fboBatch.setColor(0, 0, 0, 0.4f);
            fboBatch.draw(GameAssetManager.pixelTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            fboBatch.setColor(1, 1, 1, 1);
            fboBatch.end();

        } else {
            renderer.renderWorld(cappedDelta);
        }

        renderer.renderUI(cappedDelta);
        super.render(cappedDelta);
    }

    // --- Interfaces & Callbacks ---
    @Override
    public void onAchievementUnlocked(Achievement achievement) {
        AchievementPopup.show(this.stage, achievement);
    }
}
package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.models.Game;
import com.hollowknight.views.AnimationType;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.GameProcessor;

public class GameScreen extends AbstractScreen {
    private SpriteBatch batch;
    private Camera camera;
    private ScreenViewport viewport;
    private ShapeRenderer shapeRenderer;

    private GameProcessor gameProcessor;

    private final Game game;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        super.show();

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        shapeRenderer = new ShapeRenderer();

        gameProcessor = new GameProcessor(game);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(gameProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        game.update(delta);

        camera.position.set(game.player.position, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        AnimationType currentAnimation = game.player.currentAnimation;
        Animation<TextureRegion> animation = GameAssetManager.animationMap.get(currentAnimation);
        TextureRegion keyFrame = animation.getKeyFrame(game.player.stateTime);
        batch.draw(keyFrame,
                game.player.position.x, game.player.position.y,
                keyFrame.getRegionWidth() / 2f, 0,
                keyFrame.getRegionWidth(), keyFrame.getRegionHeight(),
                game.player.velocity.x > 0 ? -1 : 1, 1, 0);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 100, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 100);
        shapeRenderer.rect(game.player.position.x, game.player.position.y, 100, 100);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(-1000, -200, 2000, 200);
        shapeRenderer.end();

        super.render(delta);
    }
}

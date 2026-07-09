package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.views.GameAssetManager;

public abstract class AbstractScreen implements Screen {
    protected Stage stage;

    private Stack mainStack;
    protected Table rootTable;
    private Stack modalStack;
    private Stack toastStack;

    protected Skin skin;

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(0.5f);
        stage = new Stage(viewport);
        skin = GameAssetManager.skin;

        mainStack = new Stack();
        mainStack.setFillParent(true);

        modalStack = new Stack();
        toastStack = new Stack();
        rootTable = new Table();

        mainStack.add(rootTable);
        mainStack.add(modalStack);
        mainStack.add(toastStack);

        stage.addActor(mainStack);

        Gdx.input.setInputProcessor(stage);
        Gdx.graphics.setCursor(GameAssetManager.customCursor);
    }

    @Override
    public void render(float delta) {
        // 1. Act and draw the stage normally first
        stage.act(delta);
        stage.draw();

        // 2. Fetch the current brightness value (0 to 100)
        int brightness = Settings.getInstance().getBrightness();

        // 3. If brightness is less than 100, draw the darkening overlay
        if (brightness < 100) {
            // Set the absolute darkest the screen is allowed to get (e.g., 60% opacity)
            float maxDarkness = 0.6f;

            // Calculate alpha: scales between 0.0 (at 100 brightness) and maxDarkness (at 0
            // brightness)
            float alpha = (1.0f - (brightness / 100f)) * maxDarkness;

            stage.getBatch().begin();
            // Set the batch color to black with the calculated alpha transparency
            stage.getBatch().setColor(0, 0, 0, alpha);

            // Draw the single pixel texture stretched across the entire viewport
            stage.getBatch().draw(
                    GameAssetManager.pixelTexture,
                    0,
                    0,
                    stage.getViewport().getWorldWidth(),
                    stage.getViewport().getWorldHeight());

            // Always reset the batch color back to white when done to avoid tinting other
            // elements!
            stage.getBatch().setColor(Color.WHITE);
            stage.getBatch().end();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public Stack getModalStack() {
        return modalStack;
    }

    public void openToast(String message) {
        Table wrapper = new Table();
        wrapper.pad(10).right().bottom();

        Table toast = new Table();
        toast.pad(5);
        toast.setBackground(skin.getDrawable("window"));

        Label messageLabel = new Label(message, skin);

        toast.add(messageLabel).growX();

        wrapper.add(toast).minWidth(150);
        toastStack.add(wrapper);

        toast.addAction(
                Actions.sequence(
                        Actions.moveBy(0, -100),
                        Actions.moveBy(0, 100, 0.5f, Interpolation.swingOut)));

        toast.setTouchable(Touchable.enabled);
        toast.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toast.addAction(
                        Actions.sequence(
                                Actions.alpha(0, 0.75f, Interpolation.smoother),
                                Actions.run(new Runnable() {
                                    @Override
                                    public void run() {
                                        wrapper.remove();
                                    }
                                })));
            }
        });
    }
}

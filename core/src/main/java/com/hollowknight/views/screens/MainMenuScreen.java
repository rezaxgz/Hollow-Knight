package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.hollowknight.controller.AudioController;
import com.hollowknight.controller.GeneralController;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.theme.MenuThemeSkin;
import com.hollowknight.views.theme.MenuThemeType;

public class MainMenuScreen extends AbstractScreen {

    private MenuThemeSkin menuTheme;

    @Override
    public void show() {
        super.show();

        // 1. Initialize your friend's dynamic theme engine
        menuTheme = MenuThemeSkin.fromSettings();

        // 2. Prepare the root table inherited from AbstractScreen
        rootTable.clear();
        rootTable.center();
        // Lowered default padding to prevent vertical stretching
        rootTable.defaults().pad(5f);

        // 3. Add Logo and Ornament (Scaled down slightly with reduced padding)
        rootTable.add(menuTheme.createTitleLogo(380f))
                .width(380f).height(120f).padTop(10f).padBottom(5f).row();

        // 4. Inject your screens into her aesthetic buttons
        addButton("Start Game", () -> UiManager.setScreen(new StartGameScreen()));
        addButton("Settings", () -> UiManager.setScreen(new SettingsMenuScreen()));
        addButton("Guide", () -> UiManager.setScreen(new GuideMenuScreen()));
        addButton("Achievements", () -> UiManager.setScreen(new AchievementsMenuScreen()));
        addButton("Quit Game", () -> {
            GeneralController.exitApp();
            Gdx.app.exit();
        });

        // 5. Add the dynamic Theme toggler
        TextButton themeButton = menuTheme.createMenuButton("Theme: " + menuTheme.getTheme().getDisplayName());
        themeButton.getLabel().setFontScale(0.75f); // Scaled down slightly
        themeButton.getLabel().setAlignment(Align.center);
        themeButton.getLabel().setColor(0.55f, 0.62f, 0.70f, 0.82f);

        themeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cycleMenuTheme();
            }
        });

        // Reduced top padding on the theme button
        rootTable.add(themeButton).width(280f).height(36f).padTop(15f).row();

        // 6. Play Background Music
        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }

    private void cycleMenuTheme() {
        MenuThemeType next = menuTheme.getTheme().next();
        Gdx.app.getPreferences("hollow-knight-settings").putString("menuTheme", next.getId()).flush();
        // Refresh the screen to apply the new theme
        UiManager.setScreen(new MainMenuScreen());
    }

    private void addButton(String text, Runnable action) {
        TextButton button = menuTheme.createMenuButton(text);

        // Reduced font scale so it fits standard viewports without overlapping
        button.getLabel().setFontScale(1.15f);
        button.getLabel().setAlignment(Align.center);

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });

        // Reduced height slightly to 40f to ensure the whole stack stays contained
        rootTable.add(button).width(320f).height(40f).row();
    }

    @Override
    public void render(float delta) {
        // Clear standard buffers
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the dynamic animated background (soul glow particles, beams)
        if (menuTheme != null) {
            menuTheme.drawBackground(delta, false);
        }

        // Standard AbstractScreen Stage Drawing (if your stage variable is named
        // 'stage')
        // Using getStage() if rootTable provides it, or just acting on the stage.
        if (rootTable.getStage() != null) {
            rootTable.getStage().act(Math.min(delta, 1f / 30f));
            rootTable.getStage().draw();
        }
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
        super.dispose();
        if (menuTheme != null) {
            menuTheme.dispose();
        }
    }
}
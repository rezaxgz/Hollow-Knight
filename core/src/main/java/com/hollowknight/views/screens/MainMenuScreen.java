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

    // --- Core Theme ---
    private MenuThemeSkin menuTheme;

    // --- Initialization & UI Setup ---
    @Override
    public void show() {
        super.show();

        menuTheme = MenuThemeSkin.fromSettings();

        rootTable.clear();
        rootTable.center();
        rootTable.defaults().pad(5f);

        rootTable.add(menuTheme.createTitleLogo(380f))
                .width(380f).height(120f).padTop(10f).padBottom(5f).row();

        addButton("Start Game", () -> UiManager.setScreen(new StartGameScreen()));
        addButton("Settings", () -> UiManager.setScreen(new SettingsMenuScreen()));
        addButton("Guide", () -> UiManager.setScreen(new GuideMenuScreen()));
        addButton("Achievements", () -> UiManager.setScreen(new AchievementsMenuScreen()));
        addButton("Quit Game", () -> {
            GeneralController.exitApp();
            Gdx.app.exit();
        });

        TextButton themeButton = menuTheme.createMenuButton("Theme: " + menuTheme.getTheme().getDisplayName());
        themeButton.getLabel().setFontScale(0.75f);
        themeButton.getLabel().setAlignment(Align.center);
        themeButton.getLabel().setColor(0.55f, 0.62f, 0.70f, 0.82f);

        themeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cycleMenuTheme();
            }
        });

        rootTable.add(themeButton).width(280f).height(36f).padTop(15f).row();
        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }

    // --- Action Handlers ---
    private void cycleMenuTheme() {
        MenuThemeType next = menuTheme.getTheme().next();
        Gdx.app.getPreferences("hollow-knight-settings").putString("menuTheme", next.getId()).flush();
        UiManager.setScreen(new MainMenuScreen());
    }

    private void addButton(String text, Runnable action) {
        TextButton button = menuTheme.createMenuButton(text);
        button.getLabel().setFontScale(1.15f);
        button.getLabel().setAlignment(Align.center);

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });

        rootTable.add(button).width(320f).height(40f).row();
    }

    // --- Core Render Loop & Lifecycle ---
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (menuTheme != null) {
            menuTheme.drawBackground(delta, false);
        }

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
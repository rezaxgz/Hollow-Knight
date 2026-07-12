package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.gamedata.SaveManager;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.actors.SaveCard;
import com.hollowknight.views.theme.MenuThemeSkin;

public class StartGameScreen extends AbstractScreen {

    // --- Core Theme ---
    private MenuThemeSkin menuTheme;

    // --- Initialization & Layout ---
    @Override
    public void show() {
        super.show();

        menuTheme = MenuThemeSkin.fromSettings();

        rootTable.clearChildren();
        rootTable.center().top().pad(40);

        Label title = menuTheme.createTitleLabel("Select Save Slot");
        rootTable.add(title).padBottom(30).row();

        Table saveList = new Table();
        saveList.top().pad(10);
        saveList.defaults().space(20);

        for (int i = 0; i < 4; i++) {
            GameSave g = SaveManager.loadGame(i);
            SaveCard saveCard = new SaveCard(i, g, menuTheme);
            saveList.add(saveCard).growX().row();
        }

        ScrollPane scrollPane = new ScrollPane(saveList, GameAssetManager.hollowSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        rootTable.add(scrollPane).size(700, 450).padBottom(30).row();

        // Overlay table for back button anchoring
        TextButton backBtn = menuTheme.createMenuButton("Back");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        Table cornerTable = new Table();
        cornerTable.setFillParent(true);
        cornerTable.top().left();
        cornerTable.add(backBtn).pad(20);

        stage.addActor(cornerTable);
        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }

    // --- Core Render Loop ---
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (menuTheme != null) {
            menuTheme.drawBackground(delta, true);
        }

        if (stage != null) {
            stage.act(Math.min(delta, 1f / 30f));
            stage.draw();
        }
    }

    // --- Cleanup ---
    @Override
    public void dispose() {
        super.dispose();
        if (menuTheme != null) {
            menuTheme.dispose();
        }
    }
}
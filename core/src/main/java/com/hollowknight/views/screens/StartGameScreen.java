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

    private MenuThemeSkin menuTheme;

    @Override
    public void show() {
        super.show();

        // Initialize the Theme Engine[cite: 8]
        menuTheme = MenuThemeSkin.fromSettings();

        rootTable.clearChildren();
        rootTable.center().top().pad(40);

        Label title = menuTheme.createTitleLabel("Select Save Slot");
        rootTable.add(title).padBottom(30).row();

        Table saveList = new Table();
        saveList.top().pad(10);
        saveList.defaults().space(20); // Give cards more breathing room

        // Explicitly load slots 0 through 3[cite: 5]
        for (int i = 0; i < 4; i++) {
            GameSave g = SaveManager.loadGame(i);
            // Pass the menuTheme so SaveCard can style itself
            SaveCard saveCard = new SaveCard(i, g, menuTheme);
            saveList.add(saveCard).growX().row();
        }

        // Retain the standard skin for the ScrollPane to keep scrollbar graphics[cite:
        // 8]
        ScrollPane scrollPane = new ScrollPane(saveList, GameAssetManager.hollowSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        rootTable.add(scrollPane).size(700, 450).padBottom(30).row();

        TextButton backBtn = menuTheme.createMenuButton("Back");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        rootTable.add(backBtn).center();

        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }

    @Override
    public void render(float delta) {
        // Clear standard buffers[cite: 8]
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (menuTheme != null) {
            // true flag triggers the saveBackgroundTexture
            menuTheme.drawBackground(delta, true);
        }

        if (stage != null) {
            stage.act(Math.min(delta, 1f / 30f));
            stage.draw();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (menuTheme != null) {
            menuTheme.dispose(); // Prevent memory leaks[cite: 8]
        }
    }
}
package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.achievements.Achievement;
import com.hollowknight.models.achievements.AchievementManager;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.theme.MenuThemeSkin;

import java.util.Collection;

public class AchievementsMenuScreen extends AbstractScreen {

    // --- Core Theme ---
    private MenuThemeSkin menuTheme;

    // --- Initialization & Layout ---
    @Override
    public void show() {
        super.show();

        menuTheme = MenuThemeSkin.fromSettings();
        Skin customSkin = GameAssetManager.hollowSkin;

        rootTable.clearChildren();
        rootTable.center().top().pad(20);

        Label titleLabel = menuTheme.createTitleLabel("Achievements");
        rootTable.add(titleLabel).padBottom(20).row();

        Table listTable = new Table();
        listTable.defaults().pad(15).left();

        Collection<Achievement> achievements = AchievementManager.getInstance().getAchievements();

        for (Achievement ach : achievements) {
            Table rowTable = new Table();
            rowTable.align(Align.left);

            Image icon = new Image(new Texture(Gdx.files.internal(ach.iconPath)));

            if (!ach.isUnlocked()) {
                icon.setColor(0.2f, 0.2f, 0.2f, 0.4f);
            } else {
                icon.setColor(Color.WHITE);
            }

            rowTable.add(icon).size(64, 64).padRight(20);

            Table textContainer = new Table();
            textContainer.align(Align.left);

            Color titleColor = ach.isUnlocked() ? Color.GOLD : Color.GRAY;
            Color descColor = ach.isUnlocked() ? Color.WHITE : Color.DARK_GRAY;
            String statusIndicator = ach.isUnlocked() ? "" : " [LOCKED]";

            Label nameLabel = menuTheme.createBodyLabel(ach.title + statusIndicator);
            nameLabel.setColor(titleColor);
            nameLabel.setFontScale(1.1f);
            textContainer.add(nameLabel).align(Align.left).row();

            Label descLabel = menuTheme.createBodyLabel(ach.description);
            descLabel.setColor(descColor);
            descLabel.setWrap(true);
            textContainer.add(descLabel).width(450).align(Align.left).padTop(5);

            rowTable.add(textContainer).align(Align.left);
            listTable.add(rowTable).padBottom(10).row();
        }

        ScrollPane scrollPane = new ScrollPane(listTable, customSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setCancelTouchFocus(false);

        rootTable.add(scrollPane).grow().pad(20).row();

        TextButton backBtn = menuTheme.createMenuButton("Back");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        rootTable.add(backBtn).padBottom(20).center();

        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }

    // --- Core Render Loop ---
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (menuTheme != null) {
            menuTheme.drawBackground(delta, false);
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
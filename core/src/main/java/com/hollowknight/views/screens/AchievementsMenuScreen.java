package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.achievements.Achievement;
import com.hollowknight.models.achievements.AchievementManager;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;

import java.util.Collection;

public class AchievementsMenuScreen extends AbstractScreen {

    @Override
    public void show() {
        super.show();

        Skin customSkin = GameAssetManager.hollowSkin;

        rootTable.clearChildren();
        rootTable.center().top().pad(20);

        Texture backTexture = new Texture(Gdx.files.internal("menu/main menu/img_2.png"));
        rootTable.setBackground(new TextureRegionDrawable(backTexture));

        Label titleLabel = new Label("Achievements", customSkin);
        titleLabel.setFontScale(1.4f);
        rootTable.add(titleLabel).padBottom(20).row();

        Table listTable = new Table(customSkin);
        listTable.defaults().pad(15).left();

        Collection<Achievement> achievements = AchievementManager.getInstance().getAchievements();

        for (Achievement ach : achievements) {
            Table rowTable = new Table(customSkin);
            rowTable.align(Align.left);

            // 1. Setup the Icon
            Image icon = new Image(new Texture(Gdx.files.internal(ach.iconPath)));

            if (!ach.isUnlocked()) {
                // Darken and apply transparency if locked
                icon.setColor(0.2f, 0.2f, 0.2f, 0.4f);
            } else {
                // Restore to full color and opacity if unlocked
                icon.setColor(Color.WHITE);
            }

            // Add icon to the left side of the row table
            rowTable.add(icon).size(64, 64).padRight(20);

            // 2. Create a nested table for the text content
            Table textContainer = new Table(customSkin);
            textContainer.align(Align.left);

            Color titleColor = ach.isUnlocked() ? Color.GOLD : Color.GRAY;
            Color descColor = ach.isUnlocked() ? Color.WHITE : Color.DARK_GRAY;
            String statusIndicator = ach.isUnlocked() ? "" : " [LOCKED]";

            Label nameLabel = new Label(ach.title + statusIndicator, customSkin);
            nameLabel.setColor(titleColor);
            nameLabel.setFontScale(1.1f);
            textContainer.add(nameLabel).align(Align.left).row();

            Label descLabel = new Label(ach.description, customSkin);
            descLabel.setColor(descColor);
            descLabel.setWrap(true);
            textContainer.add(descLabel).width(450).align(Align.left).padTop(5);

            // Add the text container next to the icon
            rowTable.add(textContainer).align(Align.left);

            // Add the completed row to the master list table
            listTable.add(rowTable).padBottom(10).row();
        }

        ScrollPane scrollPane = new ScrollPane(listTable, customSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setCancelTouchFocus(false);

        rootTable.add(scrollPane).grow().pad(20).row();

        TextButton backBtn = new TextButton("Back", customSkin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        rootTable.add(backBtn).padBottom(20).center();

        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }
}
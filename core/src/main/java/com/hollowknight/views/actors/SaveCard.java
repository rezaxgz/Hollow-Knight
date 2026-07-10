package com.hollowknight.views.actors;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.gamedata.SaveManager;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.screens.GameScreen;
import com.hollowknight.views.screens.StartGameScreen;
import com.hollowknight.views.theme.MenuThemeSkin;

public class SaveCard extends Table {

    public SaveCard(final int slotIndex, final GameSave gameSave, final MenuThemeSkin menuTheme) {
        defaults().space(15);
        pad(15);

        // Give the card a nice semi-transparent dark background block[cite: 6]
        setBackground(menuTheme.panelDrawable(0.6f));

        // 1. Resolve and add the Image
        String regionId = (gameSave != null && gameSave.currentRegion != null) ? gameSave.currentRegion.id : null;
        Image progressImage = menuTheme.createSlotImage(regionId);
        add(progressImage).size(140, 90).left().padRight(15);

        // 2. Build the Layout
        if (gameSave == null) {
            // Empty Slot Layout[cite: 9]
            Label nameLabel = menuTheme.createBodyLabel("Slot " + slotIndex + " - Empty");
            TextButton beginBtn = menuTheme.createMenuButton("Begin");

            add(nameLabel).expandX().left();
            add(beginBtn).width(120).right();

            beginBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSave newSave = GameSave.gameStart(slotIndex);
                    UiManager.setScreen(new GameScreen(new GameWorld(newSave)));
                }
            });
        } else {
            // Filled Slot Layout[cite: 9]
            Table textTable = new Table();
            Label nameLabel = menuTheme.createSectionLabel(gameSave.getWorldName());

            // Format time nicely
            long minutes = gameSave.totalPassedTime / 60;
            Label infoLabel = menuTheme.createBodyLabel("Slot " + slotIndex + "  |  Time: " + minutes + "m");
            infoLabel.setFontScale(0.85f);

            textTable.add(nameLabel).left().row();
            textTable.add(infoLabel).left().padTop(5);

            add(textTable).expandX().left();

            TextButton loadBtn = menuTheme.createMenuButton("Load");
            TextButton deleteBtn = menuTheme.createMenuButton("Delete");

            add(loadBtn).width(100).right().padRight(10);
            add(deleteBtn).width(100).right();

            loadBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    UiManager.setScreen(new GameScreen(new GameWorld(gameSave)));
                }
            });

            deleteBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SaveManager.deleteSave(slotIndex);
                    UiManager.setScreen(new StartGameScreen());
                }
            });
        }
    }
}
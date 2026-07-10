package com.hollowknight.views.actors;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.gamedata.SaveManager;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.screens.GameScreen;
import com.hollowknight.views.screens.StartGameScreen;

public class SaveCard extends Table {

    public SaveCard(final int slotIndex, final GameSave gameSave) {
        Skin skin = GameAssetManager.skin;

        defaults().space(10);
        pad(10);

        if (gameSave == null) {
            // Empty Slot Layout
            Label nameLabel = new Label("Slot " + slotIndex + " - Empty", skin);
            TextButton beginBtn = new TextButton("Begin", skin);

            add(nameLabel).expandX().left();
            add(beginBtn).width(100).right();

            beginBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSave newSave = GameSave.gameStart(slotIndex);
                    UiManager.setScreen(new GameScreen(new GameWorld(newSave)));
                }
            });
        } else {
            // Filled Slot Layout
            Label nameLabel = new Label(gameSave.getWorldName() + " (Slot " + slotIndex + ")", skin);
            TextButton loadBtn = new TextButton("Load", skin);
            TextButton deleteBtn = new TextButton("Delete", skin);

            add(nameLabel).expandX().left();
            add(loadBtn).width(80).right();
            add(deleteBtn).width(80).right();

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
                    // Refresh the screen to show the newly emptied slot
                    UiManager.setScreen(new StartGameScreen());
                }
            });
        }
    }
}
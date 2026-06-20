package com.hollowknight.views.actors;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.models.GameWorld;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.screens.GameScreen;

public class SaveCard extends Table {
    @SuppressWarnings("unused")
    private final GameWorld gameSave;

    public SaveCard(GameWorld gameSave) {
        Skin skin = GameAssetManager.skin;
        this.gameSave = gameSave;

        defaults().space(10);
        pad(5);

        Label nameLabel = new Label(gameSave.getWorldName(), skin);
        TextButton loadBtn = new TextButton("load", skin);

        add(nameLabel);
        add(loadBtn).expandX().right().width(50);

        loadBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new GameScreen(gameSave));
            }
        });
    }
}

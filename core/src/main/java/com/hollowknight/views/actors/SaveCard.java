package com.hollowknight.views.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.models.GameSave;

public class SaveCard extends Table {
    private final GameSave gameSave;

    public SaveCard(GameSave gameSave) {
        Skin skin = GameAssetManager.skin;
        this.gameSave = gameSave;

        defaults().space(10);
        pad(5);

        Label nameLabel = new Label(gameSave.saveName(), skin);
        Label progressLabel = new Label(Integer.toString(gameSave.progress()) + "%", skin);
        TextButton loadBtn = new TextButton("load", skin);

        add(nameLabel);
        add(progressLabel);
        add(loadBtn).expandX().right().width(50);
    }
}

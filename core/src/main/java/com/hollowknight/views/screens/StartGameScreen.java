package com.hollowknight.views.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.models.GameSave;
import com.hollowknight.views.actors.SaveCard;
import com.hollowknight.views.UiManager;

public class StartGameScreen extends AbstractScreen {
    @Override
    public void show() {
        super.show();

        Stack stack = new Stack();

        Table backBtnWrapper = new Table();
        backBtnWrapper.top().left().pad(10);
        TextButton backBtn = new TextButton("back", skin);
        backBtnWrapper.add(backBtn);

        Table saveListWrapper = new Table();
        Table saveList = new Table();
        saveList.top().pad(10);
        saveList.defaults().space(10);

        saveList.setBackground(skin.getDrawable("window"));

        for (int i = 0; i < 20; i++) {
            SaveCard saveCard = new SaveCard(new GameSave("Save" + i + 1, i));
            saveList.add(saveCard).growX().row();
        }
        ScrollPane scrollPane = new ScrollPane(saveList);

        saveListWrapper.add(scrollPane).size(300, 200);

        stack.add(backBtnWrapper);
        stack.add(saveListWrapper);

        rootTable.add(stack).grow();

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });
    }
}

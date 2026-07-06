package com.hollowknight.views.screens;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.views.actors.SaveCard;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.gamedata.Loader;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.GameAssetManager;
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

        List<GameWorld> loadedSaves = Loader.loadSaves();
        for (GameWorld g : loadedSaves) {
            SaveCard saveCard = new SaveCard(g);
            saveList.add(saveCard).growX().row();
        }

        saveList.setBackground(skin.getDrawable("window"));

        TextButton newGameBtn = new TextButton("New Game", skin);

        ScrollPane scrollPane = new ScrollPane(saveList);

        saveListWrapper.add(newGameBtn)
                .width(300)
                .padBottom(10)
                .row();

        saveListWrapper.add(scrollPane)
                .size(300, 200);

        stack.add(backBtnWrapper);
        stack.add(saveListWrapper);

        rootTable.add(stack).grow();

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new GameScreen(new GameWorld(GameSave.gameStart())));
            }
        });

        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }
}

package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.models.Game;
import com.hollowknight.views.UiManager;

public class MainMenuScreen extends AbstractScreen {
    @Override
    public void show() {
        super.show();

        Stack stack = new Stack();

        Table exitBtnWrapper = new Table();
        exitBtnWrapper.bottom().left().pad(10);
        TextButton exitBtn = new TextButton("exit", skin);
        exitBtnWrapper.add(exitBtn).width(50);

        Table loginBtnWrapper = new Table();
        loginBtnWrapper.top().left().pad(10);
        TextButton loginBtn = new TextButton("login", skin);
        loginBtnWrapper.add(loginBtn).width(50);

        Table settingsBtnWrapper = new Table();
        settingsBtnWrapper.top().right().pad(10);
        TextButton settingsBtn = new TextButton("settings", skin);
        settingsBtnWrapper.add(settingsBtn).width(50);

        Table playBtnsWrapper = new Table();
        playBtnsWrapper.center().bottom().pad(10);
        playBtnsWrapper.defaults().width(100).spaceBottom(10);
        TextButton continueBtn = new TextButton("Continue", skin);
        TextButton loadBtn = new TextButton("Load", skin);
        TextButton newGameBtn = new TextButton("New Game", skin);
        playBtnsWrapper.add(continueBtn).row();
        playBtnsWrapper.add(newGameBtn).row();
        playBtnsWrapper.add(loadBtn).row();

        stack.add(exitBtnWrapper);
        stack.add(settingsBtnWrapper);
        stack.add(loginBtnWrapper);
        stack.add(playBtnsWrapper);

        rootTable.add(stack).grow();

        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new LoginMenuScreen());
            }
        });

        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        loadBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new LoadMenuScreen());
            }
        });

        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Game game = new Game();
                UiManager.setScreen(new GameScreen(game));
            }
        });
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}

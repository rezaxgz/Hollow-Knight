package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.controller.GeneralController;
import com.hollowknight.views.UiManager;

public class MainMenuScreen extends AbstractScreen {
    private void setChangeMenuScreenListener(TextButton button, AbstractScreen screen) {
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(screen);
            }
        });
    }

    @Override
    public void show() {
        super.show();

        Stack stack = new Stack();

        Table exitBtnWrapper = new Table();
        exitBtnWrapper.bottom().left().pad(10);
        TextButton exitBtn = new TextButton("exit", skin);
        exitBtnWrapper.add(exitBtn).width(50);

        Table guideBtnWrapper = new Table();
        guideBtnWrapper.top().left().pad(10);
        TextButton guideBtn = new TextButton("Guide", skin);
        guideBtnWrapper.add(guideBtn).width(50);

        Table settingsBtnWrapper = new Table();
        settingsBtnWrapper.top().right().pad(10);
        TextButton settingsBtn = new TextButton("Settings", skin);
        settingsBtnWrapper.add(settingsBtn).width(50);

        Table midBtnWrapper = new Table();
        midBtnWrapper.center().bottom().pad(10);
        midBtnWrapper.defaults().width(100).spaceBottom(10);
        TextButton startGameBtn = new TextButton("Start Game", skin);
        TextButton achievementsBtn = new TextButton("Achievements", skin);
        midBtnWrapper.add(startGameBtn).row();
        midBtnWrapper.add(achievementsBtn).row();

        stack.add(exitBtnWrapper);
        stack.add(settingsBtnWrapper);
        stack.add(guideBtnWrapper);
        stack.add(midBtnWrapper);

        rootTable.add(stack).grow();

        setChangeMenuScreenListener(guideBtn, new GuideMenuScreen());
        setChangeMenuScreenListener(achievementsBtn, new AchievementsMenuScreen());
        setChangeMenuScreenListener(startGameBtn, new StartGameScreen());
        setChangeMenuScreenListener(settingsBtn, new SettingsMenuScreen());

        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GeneralController.exitApp();
                Gdx.app.exit();
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

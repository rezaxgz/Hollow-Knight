package com.hollowknight.views.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.views.UiManager;

public class LoginMenuScreen extends AbstractScreen {
    @Override
    public void show() {
        super.show();

        Table formTable = new Table();
        formTable.defaults().space(10);

        TextField usernameField = new TextField("", skin);
        usernameField.setMessageText("username");
        TextField passwordField = new TextField("", skin);
        passwordField.setMessageText("password");

        TextButton loginBtn = new TextButton("login", skin);
        TextButton backBtn = new TextButton("back", skin);

        formTable.add(usernameField).growX().colspan(2).row();
        formTable.add(passwordField).growX().colspan(2).row();
        formTable.add(backBtn).growX();
        formTable.add(loginBtn).growX();

        rootTable.add(formTable).size(100, 200);

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (passwordField.getText().isEmpty()) {
                    openToast("password is empty");
                }
            }
        });
    }
}

package com.hollowknight.views.actors.modals;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class PauseModal extends Modal {
    public PauseModal() {
        super();

        TextButton resumeBtn = new TextButton("resume", skin);
        TextButton exitButton = new TextButton("exit", skin);

        defaults().space(5);

        add(resumeBtn).width(100).row();
        add(exitButton).width(100).row();

        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onResume();
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onExit();
            }
        });
    }

    public void onExit() {

    }

    public void onResume() {

    }
}

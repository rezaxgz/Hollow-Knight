package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.hollowknight.controller.AudioController;
import com.hollowknight.controller.GeneralController;
import com.hollowknight.views.GameAssetManager;
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

        // 1. Clear the root table and set its alignment for a centered vertical layout
        rootTable.clear();
        rootTable.center().top().pad(20);

        // 2. Apply Background Image
        // NOTE: Make sure "menu/main menu/img_2.png" exists in your assets!
        Texture backTexture = new Texture(Gdx.files.internal("menu/main menu/img_2.png"));
        rootTable.setBackground(new TextureRegionDrawable(backTexture));

        // 3. Apply Title Image
        // NOTE: Make sure "menu/main menu/img_1.png" exists in your assets!
        Texture titleTexture = new Texture(Gdx.files.internal("menu/main menu/img_1.png"));
        Image titleText = new Image(titleTexture);
        titleText.setScaling(Scaling.fit);

        // 4. Create a nested table specifically for your buttons
        Table buttonsTable = new Table();
        buttonsTable.defaults().space(10).growX();

        // 5. Initialize buttons using your custom hollowSkin!
        // This takes advantage of the TrajanPro font loaded in GameAssetManager.
        // If your JSON has a "transparent" style, you can append it like: new
        // TextButton("...", GameAssetManager.hollowSkin, "transparent");
        TextButton startGameBtn = new TextButton("Start Game", GameAssetManager.hollowSkin);
        TextButton settingsBtn = new TextButton("Settings", GameAssetManager.hollowSkin);
        TextButton guideBtn = new TextButton("Guide", GameAssetManager.hollowSkin);
        TextButton achievementsBtn = new TextButton("Achievements", GameAssetManager.hollowSkin);
        TextButton exitBtn = new TextButton("Quit Game", GameAssetManager.hollowSkin);

        // 6. Add buttons to the vertical layout
        buttonsTable.add(startGameBtn).row();
        buttonsTable.add(settingsBtn).row();
        buttonsTable.add(guideBtn).row();
        buttonsTable.add(achievementsBtn).row();
        buttonsTable.add(exitBtn).row();

        // 7. Add Title and Buttons Table to the Root Table
        rootTable.add(titleText).maxWidth(800).center().top().row();
        rootTable.add(buttonsTable).growY().minWidth(200).spaceTop(50);

        // 8. Attach Listeners
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

        // 9. Play Background Music
        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
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
        // As a best practice, if you instantiate Textures locally in show() (like
        // backTexture/titleTexture),
        // you should dispose of them here or move them into GameAssetManager so they
        // are handled centrally.
    }
}
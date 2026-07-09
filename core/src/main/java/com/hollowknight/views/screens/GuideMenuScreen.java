package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.Constants;
import com.hollowknight.models.settings.Controls;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;

public class GuideMenuScreen extends AbstractScreen {

    @Override
    public void show() {
        super.show();

        Skin customSkin = GameAssetManager.hollowSkin;
        Controls controls = Settings.getInstance().getControls();

        rootTable.clearChildren();
        rootTable.center().top().pad(20);

        // Background
        Texture backTexture = new Texture(Gdx.files.internal("menu/main menu/img_2.png"));
        rootTable.setBackground(new TextureRegionDrawable(backTexture));

        // Content Table for the ScrollPane
        Table contentTable = new Table(customSkin);
        contentTable.defaults().pad(10).left();

        // Title
        Label title = new Label("Game Guide", customSkin);
        title.setFontScale(1.4f);
        contentTable.add(title).colspan(2).center().padBottom(20);
        contentTable.row();

        // --- SECTION 1: CONTROLS ---
        addSectionTitle("Controls", contentTable, customSkin);
        addKeybindRow("Move Left", Input.Keys.toString(controls.left), contentTable, customSkin);
        addKeybindRow("Move Right", Input.Keys.toString(controls.right), contentTable, customSkin);
        addKeybindRow("Look Up", Input.Keys.toString(controls.up), contentTable, customSkin);
        addKeybindRow("Look Down", Input.Keys.toString(controls.down), contentTable, customSkin);
        addKeybindRow("Jump / Double Jump", Input.Keys.toString(controls.jump), contentTable, customSkin);
        addKeybindRow("Dash", Input.Keys.toString(controls.dash), contentTable, customSkin);
        addKeybindRow("Nail Attack", Input.Keys.toString(controls.attack), contentTable, customSkin);
        addKeybindRow("Focus (Heal)", Input.Keys.toString(controls.focus), contentTable, customSkin);
        addKeybindRow("Cast Vengeful Spirit", Input.Keys.toString(controls.cast), contentTable, customSkin);
        addKeybindRow("Cast Howling Wraiths", Input.Keys.toString(controls.scream), contentTable, customSkin);

        // --- SECTION 2: HEALTH & SOUL SYSTEM ---
        addSectionTitle("Health & Soul", contentTable, customSkin);
        addDescriptionRow("Masks (HP):",
                "You have 5 Health Masks. Taking damage removes 1 mask and grants " + Constants.INVINCIBILITY_TIME
                        + " second of invincibility. Reach 0 masks and you respawn.",
                contentTable, customSkin);
        addDescriptionRow("Soul Vessel:", "Max capacity is 99 Soul. Striking enemies with your nail grants 11 Soul.",
                contentTable, customSkin);
        addDescriptionRow("Focusing:",
                "Hold the Focus key for 1.5 seconds to consume 33 Soul and heal 1 Mask. Taking damage interrupts this.",
                contentTable, customSkin);

        // --- SECTION 3: ABILITIES ---
        addSectionTitle("Spells & Mechanics", contentTable, customSkin);
        addDescriptionRow("Spells:",
                "Vengeful Spirit shoots a horizontal projectile. Howling Wraiths unleashes an upward blast. Both cost 33 Soul and briefly lock movement.",
                contentTable, customSkin);
        addDescriptionRow("Pogo Jump:",
                "While airborne, hold DOWN and Attack to strike downward. Hitting enemies or spikes resets your Dash and Double Jump.",
                contentTable, customSkin);

        // --- SECTION 4: CHEAT CODES ---
        addSectionTitle("Cheat Codes", contentTable, customSkin);
        addDescriptionRow("Note:", "Hold [Left Ctrl] + the specified key below during gameplay to activate.",
                contentTable, customSkin);

        for (GameCheat cheat : GameCheat.values()) {
            String description = getCheatDescription(cheat);
            addKeybindRow(description, Input.Keys.toString(cheat.key), contentTable, customSkin);
        }

        // --- WRAP IN SCROLLPANE ---
        ScrollPane scrollPane = new ScrollPane(contentTable, customSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setCancelTouchFocus(false);

        rootTable.add(scrollPane).grow().pad(20).row();

        // --- BACK BUTTON ---
        TextButton backBtn = new TextButton("Back", customSkin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        rootTable.add(backBtn).padBottom(20).center();

        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }

    // Helper to add styled section headers
    private void addSectionTitle(String titleText, Table table, Skin skin) {
        Label title = new Label(titleText, skin);
        title.setFontScale(1.2f);
        title.setColor(Color.LIGHT_GRAY);
        table.add(title).colspan(2).left().padTop(25).padBottom(5);
        table.row();
    }

    // Helper for 2-column keybind layout
    private void addKeybindRow(String action, String key, Table table, Skin skin) {
        table.add(new Label(action, skin)).width(300).left();
        table.add(new Label(key, skin)).left().growX();
        table.row();
    }

    // Helper for multi-line description text
    private void addDescriptionRow(String title, String desc, Table table, Skin skin) {
        Label titleLabel = new Label(title, skin);
        titleLabel.setAlignment(Align.topLeft);

        Label descLabel = new Label(desc, skin);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.topLeft);

        table.add(titleLabel).width(150).top().left();
        table.add(descLabel).growX().top().left();
        table.row();
    }

    // Helper to map your GameCheat enums to readable descriptions based on the PDF
    // requirements
    private String getCheatDescription(GameCheat cheat) {
        return switch (cheat) {
            case TAKE_DAMAGE -> "Take 1 Damage";
            case HEAL -> "Emergency Heal";
            case FILL_SOULS -> "Refill Soul Vessel";
            case GOD_MODE -> "God Mode";
            case SPECTATOR_MODE -> "Spectator Mode";
            case KILL_ENEMIES -> "Insta-Kill Enemies";
            case TP_TO_BOSS -> "Tp to Boss";
        };
    }
}
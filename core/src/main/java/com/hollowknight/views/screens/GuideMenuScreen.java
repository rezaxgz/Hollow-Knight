package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.Constants;
import com.hollowknight.models.settings.Controls;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.theme.MenuThemeSkin;

public class GuideMenuScreen extends AbstractScreen {

    private MenuThemeSkin menuTheme; // Handles background, fonts, colors

    @Override
    public void show() {
        super.show();

        // 1. Initialize the Theme Engine
        menuTheme = MenuThemeSkin.fromSettings();

        // 2. Retain original skin strictly for interactive widgets (ScrollBars)
        Skin customSkin = GameAssetManager.hollowSkin;
        Controls controls = Settings.getInstance().getControls();

        rootTable.clearChildren();
        rootTable.center().top().pad(20);

        // Content Table for the ScrollPane (Does not need skin for layout)
        Table contentTable = new Table();
        contentTable.defaults().pad(10).left();

        // Title
        Label title = menuTheme.createTitleLabel("Game Guide");
        contentTable.add(title).colspan(2).center().padBottom(20);
        contentTable.row();

        // --- SECTION 1: CONTROLS ---
        addSectionTitle("Controls", contentTable);
        addKeybindRow("Move Left", Input.Keys.toString(controls.left), contentTable);
        addKeybindRow("Move Right", Input.Keys.toString(controls.right), contentTable);
        addKeybindRow("Look Up", Input.Keys.toString(controls.up), contentTable);
        addKeybindRow("Look Down", Input.Keys.toString(controls.down), contentTable);
        addKeybindRow("Jump / Double Jump", Input.Keys.toString(controls.jump), contentTable);
        addKeybindRow("Dash", Input.Keys.toString(controls.dash), contentTable);
        addKeybindRow("Nail Attack", Input.Keys.toString(controls.attack), contentTable);
        addKeybindRow("Focus (Heal)", Input.Keys.toString(controls.focus), contentTable);
        addKeybindRow("Cast Vengeful Spirit", Input.Keys.toString(controls.cast), contentTable);
        addKeybindRow("Cast Howling Wraiths", Input.Keys.toString(controls.scream), contentTable);

        // --- SECTION 2: HEALTH & SOUL SYSTEM ---
        addSectionTitle("Health & Soul", contentTable);
        addDescriptionRow("Masks (HP):",
                "You have 5 Health Masks. Taking damage removes 1 mask and grants " + Constants.INVINCIBILITY_TIME
                        + " second of invincibility. Reach 0 masks and you respawn.",
                contentTable);
        addDescriptionRow("Soul Vessel:", "Max capacity is 99 Soul. Striking enemies with your nail grants 11 Soul.",
                contentTable);
        addDescriptionRow("Focusing:",
                "Hold the Focus key for 1.5 seconds to consume 33 Soul and heal 1 Mask. Taking damage interrupts this.",
                contentTable);

        // --- SECTION 3: ABILITIES ---
        addSectionTitle("Spells & Mechanics", contentTable);
        addDescriptionRow("Spells:",
                "Vengeful Spirit shoots a horizontal projectile. Howling Wraiths unleashes an upward blast. Both cost 33 Soul and briefly lock movement.",
                contentTable);
        addDescriptionRow("Pogo Jump:",
                "While airborne, hold DOWN and Attack to strike downward. Hitting enemies or spikes resets your Dash and Double Jump.",
                contentTable);

        // --- SECTION 4: CHEAT CODES ---
        addSectionTitle("Cheat Codes", contentTable);
        addDescriptionRow("Note:", "Hold [Left Ctrl] + the specified key below during gameplay to activate.",
                contentTable);

        for (GameCheat cheat : GameCheat.values()) {
            String description = getCheatDescription(cheat);
            addKeybindRow(description, Input.Keys.toString(cheat.key), contentTable);
        }

        // --- WRAP IN SCROLLPANE ---
        // Using customSkin here preserves your custom scrollbar aesthetics
        ScrollPane scrollPane = new ScrollPane(contentTable, customSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setCancelTouchFocus(false);

        rootTable.add(scrollPane).grow().pad(20).row();

        // --- BACK BUTTON ---
        TextButton backBtn = menuTheme.createMenuButton("Back");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        rootTable.add(backBtn).padBottom(20).center();

        AudioController.getInstance().playBgm(GameAssetManager.menuBgm);
    }

    // Helper to add styled section headers using the Theme Skin
    private void addSectionTitle(String titleText, Table table) {
        Label title = menuTheme.createSectionLabel(titleText);
        table.add(title).colspan(2).left().padTop(25).padBottom(5);
        table.row();
    }

    // Helper for 2-column keybind layout using the Theme Skin
    private void addKeybindRow(String action, String key, Table table) {
        // Increased width from 300 to 450 to push the second column further right
        table.add(menuTheme.createBodyLabel(action)).width(450).left();
        // Added padLeft to give it a bit more breathing room
        table.add(menuTheme.createBodyLabel(key)).left().padLeft(30).growX();
        table.row();
    }

    // Helper for multi-line description text using the Theme Skin
    private void addDescriptionRow(String title, String desc, Table table) {
        Label titleLabel = menuTheme.createBodyLabel(title);
        titleLabel.setAlignment(Align.topLeft);

        Label descLabel = menuTheme.createBodyLabel(desc);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.topLeft);

        // Increased width from 150 to 250 to push the description text further right
        table.add(titleLabel).width(250).top().left();
        // Added padLeft for consistent spacing
        table.add(descLabel).growX().padLeft(30).top().left();
        table.row();
    }

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

    @Override
    public void render(float delta) {
        // Clear standard buffers
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the dynamic animated background
        if (menuTheme != null) {
            menuTheme.drawBackground(delta, false);
        }

        // Draw standard AbstractScreen stage
        if (stage != null) {
            stage.act(Math.min(delta, 1f / 30f));
            stage.draw();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (menuTheme != null) {
            menuTheme.dispose(); // Avoid memory leaks on the batch and textures
        }
    }
}
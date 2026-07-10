package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.language.Language;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;
import com.hollowknight.views.theme.MenuThemeSkin;

import java.util.EnumMap;
import java.util.Map;

public class SettingsMenuScreen extends AbstractScreen {

    private final Settings settings = Settings.getInstance();
    private MenuThemeSkin menuTheme; // Handles background, fonts, colors

    private Slider musicSlider;
    private Slider sfxSlider;
    private Slider brightnessSlider;

    private Label musicValueLabel;
    private Label sfxValueLabel;
    private Label brightnessValueLabel;

    private TextButton musicMuteBtn;
    private TextButton sfxMuteBtn;

    // Arrays used to bypass "effectively final" limitation in listeners
    private final int[] lastMusicVol = { 100 };
    private final int[] lastSfxVol = { 100 };

    private SelectBox<Language> languageSelect;

    private final Map<GameActionType, TextButton> controlButtons = new EnumMap<>(GameActionType.class);
    private GameActionType waitingForRebind = null;
    private Label rebindStatusLabel;

    @Override
    public void show() {
        super.show();

        // 1. Initialize the Theme Engine for backgrounds and text
        menuTheme = MenuThemeSkin.fromSettings();

        // 2. Retain original skin strictly for interactive widgets (Sliders,
        // ScrollBars)
        Skin customSkin = GameAssetManager.hollowSkin;

        rootTable.clearChildren();
        rootTable.center().top().pad(20);

        InputMultiplexer multiplexer = new InputMultiplexer();
        if (stage != null) {
            multiplexer.addProcessor(stage);
        }

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (waitingForRebind != null) {
                    GameActionType action = waitingForRebind;

                    settings.getControls().setControl(action, keycode);
                    controlButtons.get(action).setText(Input.Keys.toString(keycode));

                    rebindStatusLabel.setText("Bound " + action.name() + " to " + Input.Keys.toString(keycode));
                    rebindStatusLabel.setAlignment(Align.center);

                    waitingForRebind = null;

                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            rebindStatusLabel.setText("");
                        }
                    }, 3f);

                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

        Table contentTable = new Table();
        contentTable.defaults().pad(8).left();

        Label title = menuTheme.createTitleLabel("Settings");

        contentTable.add(title).colspan(4).center().padBottom(15);
        contentTable.row();

        // --- MUSIC ---
        lastMusicVol[0] = settings.getMusicLoudness() > 0 ? settings.getMusicLoudness() : 100;

        musicSlider = new Slider(0, 100, 1, false, customSkin); // Using customSkin!
        musicSlider.setValue(settings.getMusicLoudness());
        musicValueLabel = menuTheme.createBodyLabel(String.valueOf(settings.getMusicLoudness()));
        musicMuteBtn = menuTheme.createMenuButton(settings.getMusicLoudness() > 0 ? "Mute" : "Unmute");
        musicMuteBtn.getLabel().setFontScale(0.85f); // Scaled slightly to fit grid

        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                int value = Math.round(musicSlider.getValue());
                settings.setMusicLoudness(value);
                AudioController.getInstance().setMusicVolume(value);
                musicValueLabel.setText(String.valueOf(value));
                musicMuteBtn.setText(value > 0 ? "Mute" : "Unmute");
            }
        });

        musicMuteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (settings.getMusicLoudness() > 0) {
                    lastMusicVol[0] = settings.getMusicLoudness();
                    musicSlider.setValue(0);
                } else {
                    musicSlider.setValue(lastMusicVol[0]);
                }
            }
        });

        contentTable.add(menuTheme.createBodyLabel("Music")).width(160);
        contentTable.add(musicSlider).width(260);
        contentTable.add(musicValueLabel).width(40);
        contentTable.add(musicMuteBtn).width(100).padLeft(10);
        contentTable.row();

        // --- SFX ---
        lastSfxVol[0] = settings.getSfxLoudness() > 0 ? settings.getSfxLoudness() : 100;

        sfxSlider = new Slider(0, 100, 1, false, customSkin); // Using customSkin!
        sfxSlider.setValue(settings.getSfxLoudness());
        sfxValueLabel = menuTheme.createBodyLabel(String.valueOf(settings.getSfxLoudness()));
        sfxMuteBtn = menuTheme.createMenuButton(settings.getSfxLoudness() > 0 ? "Mute" : "Unmute");
        sfxMuteBtn.getLabel().setFontScale(0.85f);

        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                int value = Math.round(sfxSlider.getValue());
                settings.setSfxLoudness(value);
                AudioController.getInstance().setSfxVolume(value);
                sfxValueLabel.setText(String.valueOf(value));
                sfxMuteBtn.setText(value > 0 ? "Mute" : "Unmute");
            }
        });

        sfxMuteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (settings.getSfxLoudness() > 0) {
                    lastSfxVol[0] = settings.getSfxLoudness();
                    sfxSlider.setValue(0);
                } else {
                    sfxSlider.setValue(lastSfxVol[0]);
                }
            }
        });

        contentTable.add(menuTheme.createBodyLabel("SFX")).width(160);
        contentTable.add(sfxSlider).width(260);
        contentTable.add(sfxValueLabel).width(40);
        contentTable.add(sfxMuteBtn).width(100).padLeft(10);
        contentTable.row();

        // --- SOUND RESET ---
        TextButton resetSoundBtn = menuTheme.createMenuButton("Reset Sound Settings");
        resetSoundBtn.getLabel().setFontScale(0.9f);
        resetSoundBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicSlider.setValue(100);
                sfxSlider.setValue(100);
            }
        });

        contentTable.add(resetSoundBtn).colspan(4).left().padTop(15).padBottom(15);
        contentTable.row();

        // --- BRIGHTNESS ---
        brightnessSlider = new Slider(0, 100, 1, false, customSkin); // Using customSkin!
        brightnessSlider.setValue(settings.getBrightness());
        brightnessValueLabel = menuTheme.createBodyLabel(String.valueOf(settings.getBrightness()));

        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                int value = Math.round(brightnessSlider.getValue());
                settings.setBrightness(value);
                brightnessValueLabel.setText(String.valueOf(value));
            }
        });

        contentTable.add(menuTheme.createBodyLabel("Brightness")).width(160);
        contentTable.add(brightnessSlider).width(260);
        contentTable.add(brightnessValueLabel).width(40);
        contentTable.add().width(100).padLeft(10);
        contentTable.row();

        // --- LANGUAGE ---
        // Create a custom composite style for the SelectBox to blend old visuals with
        // new fonts
        SelectBox.SelectBoxStyle baseSelectStyle = customSkin.get(SelectBox.SelectBoxStyle.class);
        SelectBox.SelectBoxStyle selectStyle = new SelectBox.SelectBoxStyle(baseSelectStyle);
        selectStyle.font = menuTheme.getBodyFont();
        selectStyle.fontColor = menuTheme.bodyColor();

        com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle(
                baseSelectStyle.listStyle);
        listStyle.font = menuTheme.getBodyFont();
        listStyle.fontColorSelected = menuTheme.highlightColor();
        listStyle.fontColorUnselected = menuTheme.bodyColor();
        selectStyle.listStyle = listStyle;

        languageSelect = new SelectBox<>(selectStyle);
        languageSelect.setItems(Language.values());
        languageSelect.setSelected(settings.getLanguage());

        languageSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                settings.setLanguage(languageSelect.getSelected());
            }
        });

        contentTable.add(menuTheme.createBodyLabel("Language")).width(160);
        contentTable.add(languageSelect).width(260).left();
        contentTable.add().colspan(2);
        contentTable.row();

        // --- CONTROLS HEADER & RESET ---
        Label controlsTitle = menuTheme.createSectionLabel("Controls");

        TextButton resetControlsBtn = menuTheme.createMenuButton("Reset Controls");
        resetControlsBtn.getLabel().setFontScale(0.9f);
        resetControlsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.getControls().resetToDefaults();

                for (Map.Entry<GameActionType, TextButton> entry : controlButtons.entrySet()) {
                    GameActionType action = entry.getKey();
                    TextButton btn = entry.getValue();
                    int keycode = settings.getControls().getControl(action);
                    btn.setText(Input.Keys.toString(keycode));
                }

                rebindStatusLabel.setText("Controls reset to defaults.");
                rebindStatusLabel.setAlignment(Align.center);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        rebindStatusLabel.setText("");
                    }
                }, 3f);
            }
        });

        contentTable.add(controlsTitle).colspan(2).left().padTop(10);
        contentTable.add(resetControlsBtn).colspan(2).right().padTop(10);
        contentTable.row();

        rebindStatusLabel = menuTheme.createBodyLabel("");
        rebindStatusLabel.setAlignment(Align.center);
        contentTable.add(rebindStatusLabel).colspan(4).center().padBottom(8).fillX();
        contentTable.row();

        // --- CONTROLS BINDINGS ---
        addControlRow(contentTable, GameActionType.MOVE_LEFT, settings.getControls().left);
        addControlRow(contentTable, GameActionType.MOVE_RIGHT, settings.getControls().right);
        addControlRow(contentTable, GameActionType.UP, settings.getControls().up);
        addControlRow(contentTable, GameActionType.DOWN, settings.getControls().down);
        addControlRow(contentTable, GameActionType.JUMP, settings.getControls().jump);
        addControlRow(contentTable, GameActionType.ATTACK, settings.getControls().attack);
        addControlRow(contentTable, GameActionType.DASH, settings.getControls().dash);
        addControlRow(contentTable, GameActionType.FOCUS, settings.getControls().focus);
        addControlRow(contentTable, GameActionType.SCREAM, settings.getControls().scream);
        addControlRow(contentTable, GameActionType.SPRITE_CAST, settings.getControls().cast);

        // Wrap the content table in a ScrollPane using the old customSkin to retain
        // scrollbar graphics
        ScrollPane scrollPane = new ScrollPane(contentTable, customSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setCancelTouchFocus(false);

        rootTable.add(scrollPane).grow().pad(20).row();

        // Pin the Back button to the bottom
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

    private void addControlRow(Table main, GameActionType actionType, int keyCode) {
        TextButton button = menuTheme.createMenuButton(Input.Keys.toString(keyCode));
        button.getLabel().setFontScale(0.85f); // Ensures buttons fit nicely in the 4-column design
        controlButtons.put(actionType, button);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                waitingForRebind = actionType;
                button.setText("Recording...");
                rebindStatusLabel.setText("Press a key for " + actionType.name() + "...");
            }
        });

        main.add(menuTheme.createBodyLabel(actionType.name())).width(160);
        main.add(button).width(260).left();
        main.add().colspan(2);
        main.row();
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
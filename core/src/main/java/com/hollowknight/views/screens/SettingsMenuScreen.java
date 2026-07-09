package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.language.Language;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.views.GameAssetManager;
import com.hollowknight.views.UiManager;

import java.util.EnumMap;
import java.util.Map;

public class SettingsMenuScreen extends AbstractScreen {

    private final Settings settings = Settings.getInstance();

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

        Skin customSkin = GameAssetManager.hollowSkin;

        rootTable.clearChildren();
        rootTable.center().top().pad(20);

        // Set the background image to match the Main Menu
        Texture backTexture = new Texture(Gdx.files.internal("menu/main menu/img_2.png"));
        rootTable.setBackground(new TextureRegionDrawable(backTexture));

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
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

        // This table holds all the scrolling content (now set to a 4-column layout)
        Table contentTable = new Table(customSkin);
        contentTable.defaults().pad(8).left();

        Label title = new Label("Settings", customSkin);
        title.setFontScale(1.4f);

        contentTable.add(title).colspan(4).center().padBottom(15);
        contentTable.row();

        // --- MUSIC ---
        lastMusicVol[0] = settings.getMusicLoudness() > 0 ? settings.getMusicLoudness() : 100;

        musicSlider = new Slider(0, 100, 1, false, customSkin);
        musicSlider.setValue(settings.getMusicLoudness());
        musicValueLabel = new Label(String.valueOf(settings.getMusicLoudness()), customSkin);
        musicMuteBtn = new TextButton(settings.getMusicLoudness() > 0 ? "Mute" : "Unmute", customSkin);

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

        contentTable.add(new Label("Music", customSkin)).width(160);
        contentTable.add(musicSlider).width(260);
        contentTable.add(musicValueLabel).width(40);
        contentTable.add(musicMuteBtn).width(100).padLeft(10);
        contentTable.row();

        // --- SFX ---
        lastSfxVol[0] = settings.getSfxLoudness() > 0 ? settings.getSfxLoudness() : 100;

        sfxSlider = new Slider(0, 100, 1, false, customSkin);
        sfxSlider.setValue(settings.getSfxLoudness());
        sfxValueLabel = new Label(String.valueOf(settings.getSfxLoudness()), customSkin);
        sfxMuteBtn = new TextButton(settings.getSfxLoudness() > 0 ? "Mute" : "Unmute", customSkin);

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

        contentTable.add(new Label("SFX", customSkin)).width(160);
        contentTable.add(sfxSlider).width(260);
        contentTable.add(sfxValueLabel).width(40);
        contentTable.add(sfxMuteBtn).width(100).padLeft(10);
        contentTable.row();

        // --- SOUND RESET ---
        TextButton resetSoundBtn = new TextButton("Reset Sound Settings", customSkin);
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
        brightnessSlider = new Slider(0, 100, 1, false, customSkin);
        brightnessSlider.setValue(settings.getBrightness());
        brightnessValueLabel = new Label(String.valueOf(settings.getBrightness()), customSkin);

        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                int value = Math.round(brightnessSlider.getValue());
                settings.setBrightness(value);
                brightnessValueLabel.setText(String.valueOf(value));
            }
        });

        contentTable.add(new Label("Brightness", customSkin)).width(160);
        contentTable.add(brightnessSlider).width(260);
        contentTable.add(brightnessValueLabel).width(40);
        contentTable.add().width(100).padLeft(10); // Empty padding cell to maintain 4 column grid
        contentTable.row();

        // --- LANGUAGE ---
        languageSelect = new SelectBox<>(customSkin);
        languageSelect.setItems(Language.values());
        languageSelect.setSelected(settings.getLanguage());

        languageSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                settings.setLanguage(languageSelect.getSelected());
            }
        });

        contentTable.add(new Label("Language", customSkin)).width(160);
        contentTable.add(languageSelect).width(260).left();
        contentTable.add().colspan(2); // Fill remaining 2 columns
        contentTable.row();

        // --- CONTROLS HEADER & RESET ---
        Label controlsTitle = new Label("Controls", customSkin);
        controlsTitle.setFontScale(1.2f);

        TextButton resetControlsBtn = new TextButton("Reset Controls", customSkin);
        resetControlsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.getControls().resetToDefaults();

                // Dynamically update the visual text of all buttons
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

        rebindStatusLabel = new Label("", customSkin);
        rebindStatusLabel.setAlignment(Align.center);
        contentTable.add(rebindStatusLabel).colspan(4).center().padBottom(8).fillX();
        contentTable.row();

        // --- CONTROLS BINDINGS ---
        addControlRow(contentTable, GameActionType.MOVE_LEFT, settings.getControls().left, customSkin);
        addControlRow(contentTable, GameActionType.MOVE_RIGHT, settings.getControls().right, customSkin);
        addControlRow(contentTable, GameActionType.UP, settings.getControls().up, customSkin);
        addControlRow(contentTable, GameActionType.DOWN, settings.getControls().down, customSkin);
        addControlRow(contentTable, GameActionType.JUMP, settings.getControls().jump, customSkin);
        addControlRow(contentTable, GameActionType.ATTACK, settings.getControls().attack, customSkin);
        addControlRow(contentTable, GameActionType.DASH, settings.getControls().dash, customSkin);
        addControlRow(contentTable, GameActionType.FOCUS, settings.getControls().focus, customSkin);
        addControlRow(contentTable, GameActionType.SCREAM, settings.getControls().scream, customSkin);
        addControlRow(contentTable, GameActionType.SPRITE_CAST, settings.getControls().cast, customSkin);

        // Wrap the content table in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(contentTable, customSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Disable horizontal scrolling
        scrollPane.setCancelTouchFocus(false); // Keeps touch focus on sliders

        // Add the ScrollPane to the root Table
        rootTable.add(scrollPane).grow().pad(20).row();

        // Pin the Back button to the bottom of the screen (outside the scroll pane)
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

    private void addControlRow(Table main, GameActionType actionType, int keyCode, Skin skin) {
        TextButton button = new TextButton(Input.Keys.toString(keyCode), skin);
        controlButtons.put(actionType, button);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                waitingForRebind = actionType;
                button.setText("Recording...");
                rebindStatusLabel.setText("Press a key for " + actionType.name() + "...");
            }
        });

        main.add(new Label(actionType.name(), skin)).width(160);
        main.add(button).width(260).left();
        main.add().colspan(2); // Fills remaining space in the 4-column layout
        main.row();
    }
}
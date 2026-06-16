package com.hollowknight.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hollowknight.models.language.Language;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.views.UiManager;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

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

    private SelectBox<Language> languageSelect;

    private final Map<GameActionType, TextButton> controlButtons = new EnumMap<>(GameActionType.class);
    private GameActionType waitingForRebind = null;
    private Label rebindStatusLabel;

    @Override
    public void show() {
        super.show();

        rootTable.clearChildren();
        rootTable.top().pad(20);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (waitingForRebind != null) {
                    GameActionType action = waitingForRebind;

                    settings.getControls().setControl(action, keycode);
                    controlButtons.get(action).setText(action.name() + ": " + Input.Keys.toString(keycode));

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

        Table main = new Table(skin);
        main.defaults().pad(8).left();

        Label title = new Label("Settings", skin);
        title.setFontScale(1.4f);

        TextButton backBtn = new TextButton("Back", skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UiManager.setScreen(new MainMenuScreen());
            }
        });

        main.add(title).colspan(3).center().padBottom(15);
        main.row();

        // Music
        musicSlider = new Slider(0, 100, 1, false, skin);
        musicSlider.setValue(settings.getMusicLoudness());
        musicValueLabel = new Label(String.valueOf(settings.getMusicLoudness()), skin);

        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                int value = Math.round(musicSlider.getValue());
                settings.setMusicLoudness(value);
                musicValueLabel.setText(String.valueOf(value));
            }
        });

        main.add(new Label("Music", skin)).width(160);
        main.add(musicSlider).width(260);
        main.add(musicValueLabel).width(40);
        main.row();

        // SFX
        sfxSlider = new Slider(0, 100, 1, false, skin);
        sfxSlider.setValue(settings.getSfxLoudness());
        sfxValueLabel = new Label(String.valueOf(settings.getSfxLoudness()), skin);

        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                int value = Math.round(sfxSlider.getValue());
                settings.setSfxLoudness(value);
                sfxValueLabel.setText(String.valueOf(value));
            }
        });

        main.add(new Label("SFX", skin)).width(160);
        main.add(sfxSlider).width(260);
        main.add(sfxValueLabel).width(40);
        main.row();

        // Brightness
        brightnessSlider = new Slider(0, 100, 1, false, skin);
        brightnessSlider.setValue(settings.getBrightness());
        brightnessValueLabel = new Label(String.valueOf(settings.getBrightness()), skin);

        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                int value = Math.round(brightnessSlider.getValue());
                settings.setBrightness(value);
                brightnessValueLabel.setText(String.valueOf(value));
            }
        });

        main.add(new Label("Brightness", skin)).width(160);
        main.add(brightnessSlider).width(260);
        main.add(brightnessValueLabel).width(40);
        main.row();

        // Language
        languageSelect = new SelectBox<>(skin);
        languageSelect.setItems(Language.values());
        languageSelect.setSelected(settings.getLanguage());

        languageSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                settings.setLanguage(languageSelect.getSelected());
            }
        });

        main.add(new Label("Language", skin)).width(160);
        main.add(languageSelect).width(260).left();
        main.row();

        // Controls section
        Label controlsTitle = new Label("Controls", skin);
        controlsTitle.setFontScale(1.2f);
        main.add(controlsTitle).colspan(3).left().padTop(20);
        main.row();

        rebindStatusLabel = new Label("", skin);
        rebindStatusLabel.setAlignment(Align.center);
        main.add(rebindStatusLabel).colspan(3).center().padBottom(8).fillX();
        main.row();

        addControlRow(main, GameActionType.MOVE_LEFT, settings.getControls().left);
        addControlRow(main, GameActionType.MOVE_RIGHT, settings.getControls().right);
        addControlRow(main, GameActionType.JUMP, settings.getControls().jump);
        addControlRow(main, GameActionType.ATTACK, settings.getControls().attack);
        addControlRow(main, GameActionType.DASH, settings.getControls().dash);

        main.row();
        main.add(backBtn).colspan(3).right().padTop(20);

        rootTable.add(main).expand().fill();
    }

    private void addControlRow(Table main, GameActionType actionType, int keyCode) {
        TextButton button = new TextButton(actionType.name() + ": " + Input.Keys.toString(keyCode), skin);
        controlButtons.put(actionType, button);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                waitingForRebind = actionType;
                rebindStatusLabel.setText("Press a key for " + actionType.name() + "...");
            }
        });

        main.add(new Label(actionType.name(), skin)).width(160);
        main.add(button).width(260).left();
        main.add().width(40);
        main.row();
    }
}
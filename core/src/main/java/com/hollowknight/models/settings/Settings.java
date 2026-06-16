package com.hollowknight.models.settings;

import com.hollowknight.models.gamedata.Loader;
import com.hollowknight.models.language.Language;

public class Settings {
    private static Settings instance;

    private int musicLoudness = 100;
    private int sfxLoudness = 100;
    private int brightness = 100;
    private Language language = Language.ENGLISH;
    private final Controls controls = new Controls();

    public static Settings getInstance() {
        if (instance == null) {
            instance = Loader.loadSettings();
        }
        return instance;
    }

    public boolean isMusicMuted() {
        return musicLoudness == 0;
    }

    public boolean isSfxMuted() {
        return sfxLoudness == 0;
    }

    public int getMusicLoudness() {
        return musicLoudness;
    }

    public void setMusicLoudness(int musicLoudness) {
        this.musicLoudness = musicLoudness;
    }

    public int getSfxLoudness() {
        return sfxLoudness;
    }

    public void setSfxLoudness(int sfxLoudness) {
        this.sfxLoudness = sfxLoudness;
    }

    public Controls getControls() {
        return controls;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

}

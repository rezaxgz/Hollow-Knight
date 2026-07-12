package com.hollowknight.models.audio;

import com.hollowknight.models.settings.Settings;

public class AudioManager {
    private MusicManager musicManager;
    private SfxManager sfxManager;
    private float masterVolume = 1f;

    public AudioManager() {
        this.musicManager = new MusicManager();
        this.sfxManager = new SfxManager();

        // Sync the initial volumes with the loaded settings
        Settings settings = Settings.getInstance();

        // Divide by 100f to convert the 0-100 integer range to a 0.0f-1.0f float range
        float initialMusicVolume = settings.getMusicLoudness() / 100f;
        float initialSfxVolume = settings.getSfxLoudness() / 100f;

        this.musicManager.setVolume(initialMusicVolume);
        this.sfxManager.setVolume(initialSfxVolume);
    }

    public MusicManager music() {
        return musicManager;
    }

    public SfxManager sfx() {
        return sfxManager;
    }

    public void update(float delta) {
        this.musicManager.update(delta);
    }

    public void setMasterVolume(float volume) {
        this.masterVolume = volume;
        // Dynamically update music volume relative to master volume level
        this.musicManager.setVolume(this.musicManager.getVolume());
    }

    public float getMasterVolume() {
        return masterVolume;
    }
}
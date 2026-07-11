package com.hollowknight.models.audio;

public class AudioManager {
    private MusicManager musicManager;
    private SfxManager sfxManager;
    private float masterVolume = 1f;

    public AudioManager() {
        this.musicManager = new MusicManager();
        this.sfxManager = new SfxManager(); // Fixed: was instantiating AudioManager recursively
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

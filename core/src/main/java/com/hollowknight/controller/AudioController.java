package com.hollowknight.controller;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.hollowknight.models.audio.AudioManager;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.views.GameAssetManager;

// Audio and sound effects from Ronak Aboutalebi
public class AudioController {
    private static AudioController instance;
    private final AudioManager audioManager;

    private AudioController() {
        this.audioManager = new AudioManager();
    }

    public static AudioController getInstance() {
        if (instance == null) {
            instance = new AudioController();
        }
        return instance;
    }

    // --- Background Music Wrapper Methods ---
    public void playBgm(Music music) {
        if (music != null) {
            audioManager.music().play(music);
        }
    }

    public void setMusicVolume(float volume) {
        audioManager.music().setVolume(volume);
    }

    // --- Sound Effects Wrapper Methods ---
    public void playSfx(Sound sound) {
        if (sound != null) {
            audioManager.sfx().play(sound);
        }
    }

    public void playSfxLoop(Sound sound) {
        if (sound != null) {
            audioManager.sfx().playLoop(sound);
        }
    }

    public void playRandomSfx(Sound[] sounds) {
        int randomIndex = (int) (Math.random() * sounds.length);
        for (int i = 0; i < sounds.length; i++) {
            if (sounds[randomIndex + i] != null) {
                playSfx(sounds[randomIndex]);
                return;
            }
        }
    }

    public void playSfx(com.badlogic.gdx.audio.Music music) {
        if (music != null) {
            float savedSfxVolume = Settings.getInstance().getSfxLoudness();
            music.setVolume(savedSfxVolume);
            music.play();
        }
    }

    public void playRandomZoteVoice() {
        if (GameAssetManager.zoteVoices != null && GameAssetManager.zoteVoices.length > 0) {
            int randomIndex = MathUtils.random(0, GameAssetManager.zoteVoices.length - 1);
            if (GameAssetManager.zoteVoices[randomIndex] != null) {
                audioManager.sfx().play(GameAssetManager.zoteVoices[randomIndex]);
            }
        }
    }

    public void setSfxVolume(float volume) {
        audioManager.sfx().setVolume(volume);
    }

    public void transitionBgm(Music music, float duration) {
        if (music != null) {
            audioManager.music().transitionTo(music, duration);
        }
    }

    public void update(float delta) {
        audioManager.update(delta);
    }
}

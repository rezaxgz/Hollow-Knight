package com.hollowknight.models.audio;

import com.badlogic.gdx.audio.Sound;

public class SfxManager {

    private float volume = 1f;

    public void play(Sound sound) {
        sound.play(volume);
    }

    public void playLoop(Sound sound) {
        sound.loop(volume);
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

}

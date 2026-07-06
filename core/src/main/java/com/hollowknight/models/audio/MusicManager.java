package com.hollowknight.models.audio;

import com.badlogic.gdx.audio.Music;

public class MusicManager {
    private Music currentMusic;
    private float volume = 1f;

    public void play(Music music) {
        // Prevent restarting the exact same song if it's already playing
        if (currentMusic == music && currentMusic.isPlaying()) {
            return;
        }

        if (currentMusic != null)
            currentMusic.stop();

        currentMusic = music;
        currentMusic.setLooping(true);
        currentMusic.setVolume(volume);
        currentMusic.play();
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }

    public float getVolume() {
        return volume;
    }
}

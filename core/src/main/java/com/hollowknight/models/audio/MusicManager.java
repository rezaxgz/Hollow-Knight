package com.hollowknight.models.audio;

import com.badlogic.gdx.audio.Music;

public class MusicManager {
    private Music currentMusic;
    private Music nextMusic;

    private float volume = 1f;
    private float fadeDuration = 1f;
    private float currentFadeTime = 0f;

    private boolean isFadingOut = false;
    private boolean isFadingIn = false;

    public void play(Music music) {
        if (currentMusic == music && currentMusic.isPlaying()) {
            return;
        }

        // Cancel any active fades if we are forcing a play
        isFadingOut = false;
        isFadingIn = false;

        if (currentMusic != null)
            currentMusic.stop();

        currentMusic = music;
        currentMusic.setLooping(true);
        currentMusic.setVolume(volume);
        currentMusic.play();
    }

    public void transitionTo(Music music, float duration) {
        if (currentMusic == music)
            return;

        // If nothing is playing, just start the new music
        if (currentMusic == null) {
            play(music);
            return;
        }

        this.nextMusic = music;
        this.fadeDuration = duration;
        this.currentFadeTime = 0f;
        this.isFadingOut = true;
        this.isFadingIn = false;
    }

    public void update(float delta) {
        if (isFadingOut) {
            currentFadeTime += delta;
            float progress = Math.min(currentFadeTime / fadeDuration, 1f);

            if (currentMusic != null) {
                currentMusic.setVolume(volume * (1f - progress));
            }

            // Once faded out, swap tracks and start fading in
            if (progress >= 1f) {
                if (currentMusic != null)
                    currentMusic.stop();

                currentMusic = nextMusic;
                if (currentMusic != null) {
                    currentMusic.setLooping(true);
                    currentMusic.setVolume(0f);
                    currentMusic.play();
                }

                isFadingOut = false;
                isFadingIn = true;
                currentFadeTime = 0f;
            }
        } else if (isFadingIn) {
            currentFadeTime += delta;
            float progress = Math.min(currentFadeTime / fadeDuration, 1f);

            if (currentMusic != null) {
                currentMusic.setVolume(volume * progress);
            }

            // Stop fading in once full volume is reached
            if (progress >= 1f) {
                isFadingIn = false;
            }
        }
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (currentMusic != null && !isFadingOut && !isFadingIn) {
            currentMusic.setVolume(volume);
        }
    }

    public float getVolume() {
        return volume;
    }
}
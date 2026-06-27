package com.hollowknight.models.player;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.hollowknight.models.Constants;

public enum HealthMaskState {
    FULL("animation/HUD/FilledHealth.png", 1, 60f, PlayMode.LOOP),
    BREAKING("animation/HUD/BreakHealth.png", 6, 1.2f, PlayMode.NORMAL),
    EMPTY("animation/HUD/EmptyHealth.png", 1, 60f, PlayMode.NORMAL),
    HEALING("animation/HUD/HealthRefill.png", 5, Constants.HEALTH_REFIL_TIME, PlayMode.LOOP);

    public final String path;
    public final int frameCount;
    public final float frameDuration;
    public final float duration;

    public final PlayMode mode;

    HealthMaskState(String path, int frameCount, float duration, PlayMode mode) {
        this.path = path;
        this.frameCount = frameCount;
        this.duration = duration;
        this.frameDuration = duration / frameCount;
        this.mode = mode;
    }
}

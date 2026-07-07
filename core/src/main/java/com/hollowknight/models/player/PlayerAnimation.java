package com.hollowknight.models.player;

import com.hollowknight.models.Constants;
import com.hollowknight.models.enums.AnimationType;

public enum PlayerAnimation {
    IDLE("animation/Idle.png", 9, AnimationType.LOOP_PINGPONG, 0.08f),
    RUN("animation/Run.png", 13, AnimationType.LOOP_PINGPONG, 0.05f),
    DASH("animation/Dash.png", 12, Constants.DASH_DURATION, AnimationType.ONESHOT),
    JUMP("animation/Airborne.png", 12, AnimationType.LOOP_PINGPONG, 0.08f),
    DEAD("animation/Death.png", 18, Constants.PLAYER_DEATH_ANIMATION_TIME, AnimationType.ONESHOT),
    IDLE_HURT("animation/Idle Hurt.png", 12, AnimationType.LOOP_PINGPONG, 0.08f),
    FALL("animation/Airborne.png", 12, AnimationType.LOOP_PINGPONG, 0.08f, true),
    DOUBLE_JUMP("animation/Double Jump.png", 8, Constants.JUMP_SPEED / -Constants.GRAVITY, AnimationType.ONESHOT),
    FOCUS("animation/Focus Combined.png", 10, Constants.HEALTH_REFIL_TIME, AnimationType.ONESHOT),
    SLASH("animation/Slash.png", 5, Constants.SLASH_TIME, AnimationType.ONESHOT),
    SLASH_ALT("animation/SlashAlt.png", 5, Constants.SLASH_TIME, AnimationType.ONESHOT),
    UP_SLASH("animation/UpSlash.png", 5, Constants.SLASH_TIME, AnimationType.ONESHOT),
    DOWN_SLASH("animation/DownSlash.png", 5, Constants.SLASH_TIME, AnimationType.ONESHOT),
    SCREAM("animation/Scream.png", 7, Constants.SOUL_SCREAM_TIME, AnimationType.ONESHOT),
    CAST("animation/Fireball Cast.png", 9, Constants.SPIRIT_CAST_TIME, AnimationType.ONESHOT);

    public final String path;
    public int frameCount;
    public final int colCount;
    public int rowCount = 1;
    public final AnimationType animationType;
    public final float frameDuration;
    public boolean isReversed = false;
    public PlayerEffectAnimation effect = null;

    PlayerAnimation(String path, int frameCount, int colCount, int rowCount,
            AnimationType animationType, float frameDuration) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = colCount;
        this.rowCount = rowCount;
        this.animationType = animationType;
        this.frameDuration = frameDuration;
    }

    PlayerAnimation(String path, int frameCount, int colCount, int rowCount,
            AnimationType animationType, float frameDuration, boolean isReversed) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = colCount;
        this.rowCount = rowCount;
        this.animationType = animationType;
        this.frameDuration = frameDuration;
        this.isReversed = isReversed;
    }

    PlayerAnimation(String path, int frameCount,
            AnimationType animationType, float frameDuration) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = frameCount;
        this.animationType = animationType;
        this.frameDuration = frameDuration;
    }

    PlayerAnimation(String path, int frameCount,
            AnimationType animationType, float frameDuration, boolean isReversed) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = frameCount;
        this.animationType = animationType;
        this.frameDuration = frameDuration;
        this.isReversed = isReversed;
    }

    PlayerAnimation(String path, int frameCount, float totalDuration, AnimationType animationType) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = frameCount;
        this.animationType = animationType;
        this.frameDuration = totalDuration / (float) frameCount;
    }

    PlayerAnimation(String path, int frameCount, float totalDuration, AnimationType animationType,
            PlayerEffectAnimation effect) {
        this.effect = effect;
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = frameCount;
        this.animationType = animationType;
        this.frameDuration = totalDuration / (float) frameCount;
    }
}

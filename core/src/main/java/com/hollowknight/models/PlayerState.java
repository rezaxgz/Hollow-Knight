package com.hollowknight.models;

public enum PlayerState {
    IDLE("animation/Idle.png", 9, 9, 1, true, AnimationType.LOOP_PINGPONG, 0.08f),
    RUN("animation/Run.png", 13, 13, 1, true, AnimationType.LOOP_PINGPONG, 0.05f),
    DASH("animation/Dash.png", 12, 12, 1, true, AnimationType.ONESHOT, 0.2f / 12),
    JUMP("animation/Airborne.png", 12, 12, 1, true, AnimationType.LOOP_PINGPONG, 0.08f),
    DEAD("animation/Death.png", 18, 18, 1, true, AnimationType.ONESHOT, 0.08f),
    HURT("animation/Idle Hurt.png", 12, 12, 1, true, AnimationType.LOOP_PINGPONG, 0.08f),
    FALL("animation/Airborne.png", 12, 12, 1, true, AnimationType.LOOP_PINGPONG, 0.08f, true),
    DOUBLE_JUMP("animation/Double Jump.png", 8, 8, 1, false, AnimationType.ONESHOT,
            (Constants.JUMP_SPEED / -Constants.GRAVITY) / 8);

    public final String path;
    public final int frameCount;
    public final int colCount;
    public final int rowCount;
    public final boolean isPingPong;
    public final AnimationType animationType;
    public final float frameDuration;
    public boolean isReversed = false;

    PlayerState(String path, int frameCount, int colCount, int rowCount, boolean pingPong,
            AnimationType animationType, float frameDuration) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = colCount;
        this.rowCount = rowCount;
        this.isPingPong = pingPong;
        this.animationType = animationType;
        this.frameDuration = frameDuration;
    }

    PlayerState(String path, int frameCount, int colCount, int rowCount, boolean pingPong,
            AnimationType animationType, float frameDuration, boolean isReversed) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = colCount;
        this.rowCount = rowCount;
        this.isPingPong = pingPong;
        this.animationType = animationType;
        this.frameDuration = frameDuration;
        this.isReversed = isReversed;
    }
}

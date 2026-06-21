package com.hollowknight.models;

public enum PlayerState {
    IDLE("animation/Idle.png", 9, 9, 1),
    RUN("animation/Run.png", 13, 13, 1),
    DASH("animation/Dash.png", 12, 12, 1),
    JUMP("animation/Airborne.png", 12, 12, 1),
    DEAD("animation/Death.png", 18, 18, 1),
    FOCUS("animation/Focus.png", 4, 4, 1),
    HURT("animation/Idle Hurt.png", 12, 12, 1),
    FALL("animation/Double Jump.png", 12, 12, 1);

    public final String path;
    public final int frameCount;
    public final int colCount;
    public final int rowCount;

    PlayerState(String path, int frameCount, int colCount, int rowCount) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = colCount;
        this.rowCount = rowCount;
    }
}

package com.hollowknight.views;

public enum AnimationType {
    HOLLOW_KNIGHT_IDLE("animations/idle.png", 60, 8, 8),
    HOLLOW_KNIGHT_WALK("animations/walk.png", 60, 8, 8);

    public final String path;
    public final int frameCount;
    public final int colCount;
    public final int rowCount;

    AnimationType(String path, int frameCount, int colCount, int rowCount) {
        this.path = path;
        this.frameCount = frameCount;
        this.colCount = colCount;
        this.rowCount = rowCount;
    }
}

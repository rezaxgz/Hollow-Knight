package com.hollowknight.models.player.enemies;

import com.hollowknight.models.enums.AnimationType;

public enum GroundEnemyAnimations {
    CRAWLID_WALK("animation/Crawlid/Walk.png", 4, 1f, AnimationType.LOOP_PINGPONG),
    CRAWLID_TURN("animation/Crawlid/Turn.png", 2, 0.3f, AnimationType.ONESHOT),
    CRAWLID_DEATH("animation/Crawlid/Death.png", 5, 2f, AnimationType.ONESHOT);

    public final String path;
    public final int frameCount;
    public final float totalDuration;
    public final float frameDuration;

    public final AnimationType animationType;

    GroundEnemyAnimations(String path, int frameCount, float totalDuration, AnimationType type) {
        this.path = path;
        this.frameCount = frameCount;
        this.totalDuration = totalDuration;
        this.animationType = type;
        this.frameDuration = totalDuration / (float) frameCount;
    }
}

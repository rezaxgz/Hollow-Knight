package com.hollowknight.models.player.enemies;

import com.hollowknight.models.Constants;
import com.hollowknight.models.enums.AnimationType;

public enum EnemyAnimations {
    // CRAWLID
    CRAWLID_WALK("animation/Crawlid/Walk.png", 4, 1f, AnimationType.LOOP_PINGPONG),
    CRAWLID_TURN("animation/Crawlid/Turn.png", 2, Constants.CRAWLID_TURN_TIMER, AnimationType.ONESHOT),
    CRAWLID_DEATH("animation/Crawlid/Death.png", 5, 2f, AnimationType.ONESHOT),

    // Husk HornHead
    HORNHEAD_WALK("animation/Husk_Hornhead/Walk.png", 7, 1f, AnimationType.LOOP_PINGPONG),
    HORNHEAD_ATTACK_START("animation/Husk_Hornhead/Attack Anticipate.png", 5, 1f, AnimationType.ONESHOT),
    HORNHEAD_ATTACK_RUN("animation/Husk_Hornhead/Attack Lunge.png", 12, 1f, AnimationType.LOOP_PINGPONG),
    HORNHEAD_DEATH("animation/Husk_Hornhead/Death Land.png", 5, 1f, AnimationType.ONESHOT),
    HORNHEAD_IDLE("animation/Husk_Hornhead/Idle.png", 6, 1f, AnimationType.LOOP),
    HORNHEAD_TURN("animation/Husk_Hornhead/Turn.png", 2, Constants.HORNHEAD_TURN_TIMER, AnimationType.ONESHOT),
    ;

    public final String path;
    public final int frameCount;
    public final float totalDuration;
    public final float frameDuration;

    public final AnimationType animationType;

    EnemyAnimations(String path, int frameCount, float totalDuration, AnimationType type) {
        this.path = path;
        this.frameCount = frameCount;
        this.totalDuration = totalDuration;
        this.animationType = type;
        this.frameDuration = totalDuration / (float) frameCount;
    }
}

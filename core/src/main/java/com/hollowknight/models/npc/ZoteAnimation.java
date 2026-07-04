package com.hollowknight.models.npc;

import com.hollowknight.models.Constants;
import com.hollowknight.models.enums.AnimationType;

public enum ZoteAnimation {
    ATTACK("animation/Zote/Attack.png", 4, Constants.ZOTE_ATTACK_TIME, AnimationType.ONESHOT),
    IDLE("animation/Zote/Idle.png", 5, 1.5f, AnimationType.LOOP_PINGPONG),
    TALK("animation/Zote/Talk.png", 5, 1.5f, AnimationType.LOOP_PINGPONG);

    public final String path;
    public final int frameCount;
    public final float totalDuration;
    public final float frameDuration;
    public final AnimationType type;

    ZoteAnimation(String path, int frameCount, float totalDuration, AnimationType type) {
        this.path = path;
        this.frameCount = frameCount;
        this.totalDuration = totalDuration;
        this.frameDuration = totalDuration / (float) frameCount;
        this.type = type;
    }
}

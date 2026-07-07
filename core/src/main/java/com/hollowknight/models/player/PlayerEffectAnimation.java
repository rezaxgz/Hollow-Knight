package com.hollowknight.models.player;

import com.hollowknight.models.Constants;

public enum PlayerEffectAnimation {
    SLASH("animation/Effects/SlashEffect.png", 6, Constants.SLASH_TIME, 50, 0, -1),
    SLASH_ALT("animation/Effects/SlashEffectAlt.png", 6, Constants.SLASH_TIME, 0, 0, -1),
    DOWN_SLASH("animation/Effects/DownSlashEffect.png", 6, Constants.SLASH_TIME, 0, 0),
    UP_SLASH("animation/Effects/UpSlashEffect.png", 6, Constants.SLASH_TIME, 0, 0),
    SOUL_SCREAM("animation/Effects/SoulScream.png", 13, Constants.SOUL_SCREAM_TIME, 0,
            Constants.PLAYER_HITBOX_HEIGHT - 20),
    BLAST("animation/Effects/BlastSoul.png", 8, Constants.SPIRIT_CAST_TIME, 0, -100),
    SOUL_BALL("animation/Projectile/SoulBall.png", 4, Constants.SPIRIT_CAST_TIME, 0, 0),
    SOUL_BALL_END("animation/Projectile/Ball End.png", 3, Constants.SPIRIT_CAST_TIME, 0, 0),
    DASH("animation/Effects/Dash Effect.png", 8, Constants.DASH_DURATION, -200, -30);

    public final String path;
    public final int frameCount;
    public final float duration;
    public final int xOffset;
    public final int yOffset;
    public final float frameDuration;
    public final int sclaeX;

    PlayerEffectAnimation(String path, int frameCount, float duration, int xOffset, int yOffset) {
        this.path = path;
        this.frameCount = frameCount;
        this.duration = duration;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.frameDuration = duration / (float) frameCount;
        this.sclaeX = 1;
    }

    PlayerEffectAnimation(String path, int frameCount, float duration, int xOffset, int yOffset, int scaleX) {
        this.path = path;
        this.frameCount = frameCount;
        this.duration = duration;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.frameDuration = duration / (float) frameCount;
        this.sclaeX = scaleX;
    }
}

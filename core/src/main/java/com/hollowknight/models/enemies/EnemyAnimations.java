package com.hollowknight.models.enemies;

import com.hollowknight.models.Constants;
import com.hollowknight.models.enums.AnimationType;

public enum EnemyAnimations {
    // CRAWLID
    CRAWLID_WALK("animation/Crawlid/Walk.png", 4, 1f, AnimationType.LOOP_PINGPONG),
    CRAWLID_TURN("animation/Crawlid/Turn.png", 2, Constants.GROUND_ENEMY_TURN_TIMER, AnimationType.ONESHOT),
    CRAWLID_DEATH("animation/Crawlid/Death.png", 5, 1f, AnimationType.ONESHOT),
    // Mosscreep
    MOSSCREEP_WALK("animation/Mosscreep/Walk.png", 3, 1f, AnimationType.LOOP_PINGPONG),
    MOSSCREEP_TURN("animation/Mosscreep/Turn.png", 3, Constants.GROUND_ENEMY_TURN_TIMER, AnimationType.ONESHOT),
    MOSSCREEP_DEATH("animation/Mosscreep/Death.png", 6, 1f, AnimationType.ONESHOT),

    // Mossfly
    MOSSFLY_SHAKE("animation/Mossfly/Shake.png", 3, 1f, AnimationType.LOOP_PINGPONG),
    MOSSFLY_APPEAR("animation/Mossfly/Appear.png", 6, 2f, AnimationType.ONESHOT),
    MOSSFLY_FLY("animation/Mossfly/Fly.png", 4, 1.5f, AnimationType.LOOP_PINGPONG),
    MOSSFLY_DEATH("animation/Mossfly/Death.png", 6, 2f, AnimationType.ONESHOT),

    // Husk HornHead
    HORNHEAD_WALK("animation/Husk_Hornhead/Walk.png", 7, 1f, AnimationType.LOOP_PINGPONG),
    HORNHEAD_ATTACK_START("animation/Husk_Hornhead/Attack Anticipate.png", 5, 1f, AnimationType.ONESHOT),
    HORNHEAD_ATTACK_RUN("animation/Husk_Hornhead/Attack Lunge.png", 12, 1f, AnimationType.LOOP_PINGPONG),
    HORNHEAD_DEATH("animation/Husk_Hornhead/Death Land.png", 8, 2f, AnimationType.ONESHOT),
    HORNHEAD_IDLE("animation/Husk_Hornhead/Idle.png", 6, 1f, AnimationType.LOOP),
    HORNHEAD_TURN("animation/Husk_Hornhead/Turn.png", 2, Constants.HORNHEAD_TURN_TIMER, AnimationType.ONESHOT),

    // Crystal Gaurdian
    CRYSTALLIZED_IDLE("animation/Crystallized/Idle.png", 5, 1.5f, AnimationType.LOOP_PINGPONG),
    CRYSTALLIZED_SHOOT("animation/Crystallized/Shoot.png", 7, 1.5f, AnimationType.ONESHOT),
    CRYSTALLIZED_RUN("animation/Crystallized/Run.png", 6, 1.5f, AnimationType.LOOP_PINGPONG),
    CRYSTALLIZED_TURN("animation/Crystallized/Turn.png", 3, 0.1f, AnimationType.ONESHOT),
    CRYSTALLIZED_DEATH("animation/Crystallized/Death.png", 6, 1.5f, AnimationType.ONESHOT),

    // False Knight
    FALSE_KNIGHT_IDLE("animation/False_knight/Idle.png", 5, 1f, AnimationType.LOOP_PINGPONG),
    FALSE_KNIGHT_ATTACK_ANTICIPATE("animation/False_knight/Attack Antic.png", 6, 1f, AnimationType.ONESHOT),
    FALSE_KNIGHT_ATTACK("animation/False_knight/Attack.png", 3, 0.5f, AnimationType.ONESHOT),
    FALSE_KNIGHT_ATTACK_RECOVER("animation/False_knight/Attack Recover.png", 5, 1f, AnimationType.ONESHOT),
    FALSE_KNIGHT_JUMP_ANTICIPATE("animation/False_knight/Jump Antic.png", 3, 1f, AnimationType.ONESHOT),
    FALSE_KNIGHT_JUMP_ATTACK("animation/False_knight/JumpAttack.png", 12, 2f, AnimationType.ONESHOT),
    FALSE_KNIGHT_JUMP("animation/False_knight/Jump.png", 4, 0.6f, AnimationType.ONESHOT),
    FALSE_KNIGHT_LAND("animation/False_knight/Land.png", 5, 1f, AnimationType.ONESHOT),
    FALSE_KNIGHT_RUN_ANTICIPATE("animation/False_knight/Run Antic.png", 2, 1f, AnimationType.ONESHOT),
    FALSE_KNIGHT_RUN("animation/False_knight/Run.png", 5, 1f, AnimationType.LOOP_PINGPONG),
    FALSE_KNIGHT_TURN("animation/False_knight/Turn.png", 2, 0.1f, AnimationType.ONESHOT),
    FALSE_KNIGHT_STUN_RECOVER("animation/False_knight/Stun Recover.png", 6, 2f, AnimationType.ONESHOT),
    FALSE_KNIGHT_DEATH("animation/False_knight/Death.png", 14, 1.5f, AnimationType.ONESHOT),
    FALSE_KNIGHT_DEATH_HIT("animation/False_knight/DeathHit.png", 3, 0.7f, AnimationType.ONESHOT),
    FALSE_KNIGHT_SHOCKWAVE("animation/Effects/Shockwave.png", 8, 2.5f, AnimationType.ONESHOT);

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

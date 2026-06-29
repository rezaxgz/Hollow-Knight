package com.hollowknight.models;

public class Constants {
    // General
    public static final int RIGHT_DIRECTION = 1;
    public static final int LEFT_DIRECTION = -1;
    // Player
    public static final float GRAVITY = -1000f;
    public static final float PLAYER_MOVE_SPEED = 500f;
    public static final float DASH_SPEED = 1300f;
    public static final float JUMP_SPEED = 470f;
    public static final float DASH_DURATION = 0.2f;
    public static final float DASH_COOLDOWN = 0.4f;
    public static final float INVINCIBILITY_TIME = 1.0f;
    public static final int PLAYER_HITBOX_WIDTH = 65;
    public static final int PLAYER_HITBOX_HEIGHT = 130;

    // Player Vitals
    public static final int MAX_PLAYER_HEALTH = 5;
    public static final int MAX_PLAYER_SOULS = 99;
    public static final float HEALTH_REFIL_TIME = 1.5f;
    public static final int HEALING_COST_IN_SOULS = 49;
    public static final int SUCCESSFUL_ATTACK_SOUL_BONUS = 11;
    public static float SOULS_CHANGE_TIMER = 1f;

    // Crawlid
    public static final int CRAWLID_HITBOX_WIDTH = 110;
    public static final int CRAWLID_HITBOX_HEIGHT = 80;
    public static final int CRAWLID_SPEED = 250;
    public static final float CRAWLID_TURN_TIMER = 0.08f;
    public static final float CRAWLID_DEATH_TIMER = 1f;

    // debug
    public static int a = 0;
    public static int b = 0;
}

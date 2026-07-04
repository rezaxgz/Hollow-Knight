package com.hollowknight.models;

public class Constants {
    // General
    public static final int RIGHT_DIRECTION = 1;
    public static final int LEFT_DIRECTION = -1;
    public static final int UP_DIRECTION = -1;
    public static final int DOWN_DIRECTION = 1;

    // Map
    public static final int HAZARD_DAMAGE = 1;

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
    public static final float PLAYER_DEATH_ANIMATION_TIME = 1.44f;
    public static final float PLAYER_DEATH_TIME = 3;

    // Player Combat
    public static final float SLASH_TIME = 0.5f;
    public static final float SLASH_COOLDOWN = 3f;
    public static final int PLAYER_SLASH_DAMAGE = 15;
    public static final float KNOCKBACK_DURATION = 0.2f; // How long the player loses control
    public static final float KNOCKBACK_SPEED_X = 500f; // Horizontal push force
    public static final float KNOCKBACK_SPEED_Y = 430f; // Vertical pop-up force
    public static final int ATTACK_HIT_SOULS_BONUS = 11;

    // Player Vitals
    public static final int MAX_PLAYER_HEALTH = 5;
    public static final int MAX_PLAYER_SOULS = 99;
    public static final float HEALTH_REFIL_TIME = 1.5f;
    public static final int HEALING_COST_IN_SOULS = 49;
    public static final int SUCCESSFUL_ATTACK_SOUL_BONUS = 11;
    public static float SOULS_CHANGE_TIMER = 1f;

    // Player Abilities
    public static final float SOUL_SCREAM_TIME = 1f;
    public static final int ABILITY_COST = MAX_PLAYER_SOULS / 3;
    public static final int SOUL_SCREAM_TICK_DAMAGE = 10;
    public static final int SOUL_SCREAM_HITBOX_HEIGHT = 260;
    public static final int SOUL_SCREAM_HITBOX_WIDTH = 260;

    // General Enemy Logic
    public static final float ENEMY_ACTIVE_RADIUS = 1500f;
    public static final float ENEMY_RESPAWN_RADIUS = 3000f;
    public static final float ENEMY_IGNORE_RADIUS = 3200f;
    public static final float ENEMY_KNOCKBACK_DURATION = 0.15f; // Duration of control loss
    public static final float ENEMY_KNOCKBACK_SPEED_X = 500f; // Horizontal push force
    public static final float ENEMY_KNOCKBACK_SPEED_Y = 400f; // Vertical pop force (lift)

    // Ground Enemy
    public static final int GROUND_ENEMY_HITBOX_WIDTH = 110;
    public static final int GROUND_ENEMY_HITBOX_HEIGHT = 80;
    public static final int GROUND_ENEMY_SPEED = 250;
    public static final float GROUND_ENEMY_TURN_TIMER = 0.08f;
    public static final float GROUND_ENEMY_DEATH_TIMER = 1f;
    public static final int GROUND_ENEMY_HP = 20;

    // HornHead
    public static final int HORNHEAD_HITBOX_WIDTH = 110;
    public static final int HORNHEAD_HITBOX_HEIGHT = 150;
    public static final int HORNHEAD_HITBOX_HEIGHT_ATTACKING = 120;
    public static final int HORNHEAD_SPEED = 250;
    public static final int HORNHEAD_ATTACK_SPEED = 500;
    public static final float HORNHEAD_TURN_TIMER = 0.08f;
    public static final float HORNHEAD_DEATH_TIMER = 1f;
    public static final float HORNHEAD_REST_TIMER = 4f;
    public static final float HORNHEAD_WALK_TIMER = 10f;
    public static final int HORNHEAD_HP = 50;

    // Mossfly
    public static final int MOSSFLY_HP = 30;
    public static final int MOSSFLY_SPEED = 180;
    public static final int MOSSFLY_HITBOX_WIDTH = 100;
    public static final int MOSSFLY_HITBOX_HEIGHT = 50;

    // Crystal Gaurdian
    public static final int GUARDIAN_HP = 60;
    public static final int GUARDIAN_SPEED = 550;
    public static final int GUARDIAN_HITBOX_HEIGHT = 200;
    public static final int GUARDIAN_HITBOX_WIDTH = 100;

    // debug
    public static int a = 0;
    public static int b = 0;
}

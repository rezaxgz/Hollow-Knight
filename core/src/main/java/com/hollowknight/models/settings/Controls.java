package com.hollowknight.models.settings;

import com.badlogic.gdx.Input;

public class Controls {
    public static final int DEFAULT_LEFT = Input.Keys.LEFT;
    public static final int DEFAULT_RIGHT = Input.Keys.RIGHT;
    public static final int DEFAULT_UP = Input.Keys.UP;
    public static final int DEFAULT_DOWN = Input.Keys.DOWN;
    public static final int DEFAULT_JUMP = Input.Keys.SPACE;
    public static final int DEFAULT_ATTACK = Input.Keys.X;
    public static final int DEFAULT_DASH = Input.Keys.C;
    public static final int DEFAULT_FOCUS = Input.Keys.A;
    public static final int DEFAULT_SCREAM = Input.Keys.S;
    public static final int DEFAULT_CAST = Input.Keys.D;
    public static final int DEFAULT_INTERACT = Input.Keys.E;

    public int left = Input.Keys.LEFT;
    public int right = Input.Keys.RIGHT;
    public int up = Input.Keys.UP;
    public int down = Input.Keys.DOWN;
    public int jump = Input.Keys.SPACE;
    public int attack = Input.Keys.X;
    public int dash = Input.Keys.C;
    public int focus = Input.Keys.A;
    public int scream = Input.Keys.S;
    public int cast = Input.Keys.D;
    public int interact = Input.Keys.E;

    public void resetToDefaults() {
        left = DEFAULT_LEFT;
        right = DEFAULT_RIGHT;
        up = DEFAULT_UP;
        down = DEFAULT_DOWN;
        jump = DEFAULT_JUMP;
        attack = DEFAULT_ATTACK;
        dash = DEFAULT_DASH;
        focus = DEFAULT_FOCUS;
        scream = DEFAULT_SCREAM;
        cast = DEFAULT_CAST;
        interact = DEFAULT_INTERACT;
    }

    public void setControl(GameActionType actionType, int key) {
        switch (actionType) {
            case MOVE_LEFT -> left = key;
            case MOVE_RIGHT -> right = key;
            case JUMP -> jump = key;
            case ATTACK -> attack = key;
            case DASH -> dash = key;
            case FOCUS -> focus = key;
            case DOWN -> down = key;
            case UP -> up = key;
            case SCREAM -> scream = key;
            case SPRITE_CAST -> cast = key;
        }
    }

    public int getControl(GameActionType actionType) {
        return switch (actionType) {
            case MOVE_LEFT -> left;
            case MOVE_RIGHT -> right;
            case UP -> up;
            case DOWN -> down;
            case JUMP -> jump;
            case ATTACK -> attack;
            case DASH -> dash;
            case FOCUS -> focus;
            case SCREAM -> scream;
            case SPRITE_CAST -> cast;
        };
    }

    public GameActionType getAction(int key) {
        if (key == left)
            return GameActionType.MOVE_LEFT;
        if (key == right)
            return GameActionType.MOVE_RIGHT;
        if (key == jump)
            return GameActionType.JUMP;
        if (key == attack)
            return GameActionType.ATTACK;
        if (key == dash)
            return GameActionType.DASH;
        if (key == focus)
            return GameActionType.FOCUS;
        if (key == up)
            return GameActionType.UP;
        if (key == down)
            return GameActionType.DOWN;
        if (key == scream)
            return GameActionType.SCREAM;
        if (key == cast)
            return GameActionType.SPRITE_CAST;
        return null;
    }

    public GameCheat getCheat(int key) {
        for (final GameCheat cheat : GameCheat.values()) {
            if (cheat.key == key) {
                return cheat;
            }
        }
        return null;
    }
}

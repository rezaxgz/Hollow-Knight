package com.hollowknight.models.settings;

import com.badlogic.gdx.Input;

public class Controls {
    public int left = Input.Keys.LEFT;
    public int right = Input.Keys.RIGHT;
    public int up = Input.Keys.UP;
    public int down = Input.Keys.DOWN;
    public int jump = Input.Keys.SPACE;
    public int attack = Input.Keys.X;
    public int dash = Input.Keys.C;
    public int focus = Input.Keys.A;

    // cheats (activated with key + Ctrl)
    public int takeDamage = Input.Keys.NUMPAD_SUBTRACT;
    public int heal = Input.Keys.NUMPAD_ADD;
    public int fillSouls = Input.Keys.UP;
    public int godMode = Input.Keys.G;
    public int specMode = Input.Keys.S;

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
            return GameActionType.DOWN;
        if (key == down)
            return GameActionType.UP;
        return null;
    }

    public GameCheat getCheat(int key) {
        if (key == takeDamage)
            return GameCheat.TAKE_DAMAGE;
        if (key == heal)
            return GameCheat.HEAL;
        if (key == fillSouls)
            return GameCheat.FILL_SOULS;
        if (key == godMode)
            return GameCheat.GOD_MODE;
        if (key == specMode)
            return GameCheat.SPECTATOR_MODE;
        return null;
    }
}

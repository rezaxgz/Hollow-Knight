package com.hollowknight.models.settings;

import com.badlogic.gdx.Input;

public class Controls {
    public int left = Input.Keys.A;
    public int right = Input.Keys.D;
    public int up = Input.Keys.W;
    public int down = Input.Keys.S;
    public int jump = Input.Keys.SPACE;
    public int attack = Input.Keys.X;
    public int dash = Input.Keys.C;

    public void setControl(GameActionType actionType, int key) {
        switch (actionType) {
            case MOVE_LEFT -> left = key;
            case MOVE_RIGHT -> right = key;
            case JUMP -> jump = key;
            case ATTACK -> attack = key;
            case DASH -> dash = key;
        }
    }

    public int getControl(GameActionType actionType) {
        return switch (actionType) {
            case MOVE_LEFT -> left;
            case MOVE_RIGHT -> right;
            case JUMP -> jump;
            case ATTACK -> attack;
            case DASH -> dash;
        };
    }
}

package com.hollowknight.models.settings;

import com.badlogic.gdx.Input;

public enum GameCheat {
    TAKE_DAMAGE(Input.Keys.NUMPAD_SUBTRACT),
    HEAL(Input.Keys.NUMPAD_ADD),
    FILL_SOULS(Input.Keys.UP),
    GOD_MODE(Input.Keys.G),
    SPECTATOR_MODE(Input.Keys.S),
    KILL_ENEMIES(Input.Keys.K),
    TP_TO_BOSS(Input.Keys.T);

    public final int key;

    GameCheat(int key) {
        this.key = key;
    }
}

package com.hollowknight.models.player;

public class ActiveEffect {
    public final PlayerEffectAnimation type;
    public float timer;
    public final int direction;

    public ActiveEffect(PlayerEffectAnimation type, int direction) {
        this.type = type;
        this.direction = direction;
        this.timer = 0;
    }

    public void update(float delta) {
        timer += delta;
    }

    public boolean isFinished() {
        return timer >= type.duration;
    }
}
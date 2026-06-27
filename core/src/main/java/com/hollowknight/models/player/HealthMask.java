package com.hollowknight.models.player;

public class HealthMask {
    public HealthMaskState state;
    public float stateTime = 0;

    public HealthMask(HealthMaskState state) {
        this.state = state;
    }

    public void set(HealthMaskState s) {
        state = s;
        stateTime = 0;
    }

    public void update(float delta) {
        stateTime += delta;
        if (state == HealthMaskState.FULL || state == HealthMaskState.EMPTY)
            return;
        if (stateTime >= state.duration) {
            if (state == HealthMaskState.HEALING) {
                set(HealthMaskState.FULL);
            } else if (state == HealthMaskState.BREAKING) {
                set(HealthMaskState.EMPTY);
            }
        }
    }

    public boolean isFull() {
        return this.state == HealthMaskState.FULL;
    }

    public void breakMask() {
        if (!isFull())
            return;
        set(HealthMaskState.BREAKING);
    }
}

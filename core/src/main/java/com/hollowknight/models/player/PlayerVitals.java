package com.hollowknight.models.player;

import java.util.ArrayList;
import java.util.List;

import com.hollowknight.models.Constants;

public class PlayerVitals {
    private List<HealthMask> health;
    private int souls = 80;

    public PlayerVitals() {
        this.health = new ArrayList<>();
        for (int i = 0; i < Constants.MAX_PLAYER_HEALTH; i++) {
            this.health.add(new HealthMask(HealthMaskState.FULL));
        }
    }

    public int getHealth() {
        int i = 0;
        for (HealthMask h : health) {
            if (h.state == HealthMaskState.FULL)
                i++;
        }
        return i;
    }

    public boolean isDead() {
        return getHealth() == 0;
    }

    public List<HealthMask> getHealthMasks() {
        return health;
    }

    public int getSouls() {
        return souls;
    }

    public void addSouls(int amount) {
        souls = Math.clamp(souls + amount, 0, 99);
    }

    public void update(float delta) {
        for (HealthMask h : health) {
            h.update(delta);
        }
    }

    public void takeDamage() {
        for (int i = Constants.MAX_PLAYER_HEALTH - 1; i >= 0; i--) {
            HealthMask h = health.get(i);
            if (h.isFull()) {
                h.breakMask();
                break;
            }
        }
    }
}

package com.hollowknight.models.player;

import java.util.ArrayList;
import java.util.List;

import com.hollowknight.controller.AudioController;
import com.hollowknight.models.Constants;
import com.hollowknight.views.GameAssetManager;

public class PlayerVitals {
    private List<HealthMask> health;
    private int souls = 80;
    private SoulsAnimation soulsAnimation = null;

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

    public int getSoulsInAnimation() {
        if (soulsAnimation != null) {
            return soulsAnimation.getSouls();
        }
        return souls;
    }

    public void addSouls(int amount) {
        int target = Math.clamp(souls + amount, 0, 99);
        soulsAnimation = new SoulsAnimation(getSoulsInAnimation(), target, Constants.SOULS_CHANGE_TIMER);
        souls = target;
        if (target > souls)
            AudioController.getInstance().playRandomSfx(GameAssetManager.soulSfxs);
    }

    public void update(float delta) {
        for (HealthMask h : health) {
            h.update(delta);
        }

        if (soulsAnimation != null) {
            soulsAnimation.update(delta);
            if (soulsAnimation.isOver()) {
                soulsAnimation = null;
            }
        }
    }

    public void takeDamage(int amount) {
        for (int i = 0; i < amount; i++) {
            takeDamage();
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

    public boolean canHeal() {
        for (HealthMask h : health) {
            if (!h.isFull() && h.state != HealthMaskState.HEALING)
                return true;
        }
        return false;
    }

    public void heal(int amount) {
        for (HealthMask h : health) {
            if (h.state == HealthMaskState.EMPTY || h.state == HealthMaskState.BREAKING) {
                h.set(HealthMaskState.HEALING);
                amount--;
                if (amount == 0)
                    break;
            }
        }
    }

    public void setNewAnimation(int from, int to, float time) {
        soulsAnimation = new SoulsAnimation(from, to, time);
    }

    public void resetSouls() {
        soulsAnimation = new SoulsAnimation(getSoulsInAnimation(), getSouls(), Constants.SOULS_CHANGE_TIMER);
    }
}

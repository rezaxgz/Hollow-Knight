package com.hollowknight.models.world;

import com.badlogic.gdx.math.Rectangle;

public class Obstacle extends Rectangle {
    private final boolean isInstantDeath;

    public Obstacle(boolean isInstantDeath) {
        this.isInstantDeath = isInstantDeath;
    }

    public Obstacle(Rectangle rect, boolean isInstantDeath) {
        super(rect);
        this.isInstantDeath = isInstantDeath;
    }

    public Obstacle(float x, float y, float width, float height, boolean isInstantDeath) {
        super(x, y, width, height);
        this.isInstantDeath = isInstantDeath;
    }

    public boolean isInstantDeath() {
        return this.isInstantDeath;
    }

}

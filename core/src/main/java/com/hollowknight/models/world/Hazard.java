package com.hollowknight.models.world;

import com.badlogic.gdx.math.Rectangle;

public class Hazard {
    private final Rectangle bounds;
    private final boolean instantDeath;

    public Hazard(Rectangle bounds, boolean instantDeath) {
        this.bounds = new Rectangle(bounds);
        this.instantDeath = instantDeath;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isInstantDeath() {
        return instantDeath;
    }
}

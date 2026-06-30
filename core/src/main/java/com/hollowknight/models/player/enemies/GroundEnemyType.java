package com.hollowknight.models.player.enemies;

import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.models.Constants;

public enum GroundEnemyType {
    CRAWLID(Constants.CRAWLID_SPEED, Constants.CRAWLID_HITBOX_WIDTH, Constants.CRAWLID_HITBOX_HEIGHT,
            EnemyAnimations.CRAWLID_WALK,
            EnemyAnimations.CRAWLID_TURN,
            EnemyAnimations.CRAWLID_DEATH);

    public final float speed;
    public final float width;
    public final float height;
    public final EnemyAnimations walkAnimation;
    public final EnemyAnimations turnAnimation;
    public final EnemyAnimations deathAnimation;

    GroundEnemyType(float speed, int width, int height, EnemyAnimations walkAnimation,
            EnemyAnimations turnAnimation,
            EnemyAnimations deathAnimation) {
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.turnAnimation = turnAnimation;
        this.walkAnimation = walkAnimation;
        this.deathAnimation = deathAnimation;
    }

    public Rectangle getHitbox(float x, float y) {
        return new Rectangle(x, y, width, height);
    }

    public static GroundEnemyType fromInt(int n) {
        return values()[n % values().length];
    }
}

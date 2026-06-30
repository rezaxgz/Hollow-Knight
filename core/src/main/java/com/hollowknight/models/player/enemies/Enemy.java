package com.hollowknight.models.player.enemies;

import java.util.List;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public abstract class Enemy {
    public final Vector2 respawnPosition;
    public Vector2 position;
    public Vector2 velocity;

    public EnemyAnimations animation;
    public float animationTime = 0;

    public int facingDirection = Constants.RIGHT_DIRECTION;
    public boolean isOnGround = false;
    public boolean isDead = false;

    public Enemy(Vector2 pos) {
        this.position = new Vector2(pos);
        this.respawnPosition = new Vector2(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
    }

    // Every unique enemy must implement these methods
    public abstract void update(float delta, Player player, List<Rectangle> solidBlocks);

    public abstract Rectangle getBounds();

    public void kill() {
        this.isDead = true;
        this.velocity.set(0, 0);
    }

    public void respawn() {
        this.position.set(respawnPosition);
        this.velocity.set(0, Constants.GRAVITY);
        this.facingDirection = Constants.RIGHT_DIRECTION;
        this.isOnGround = false;
        this.isDead = false;
        this.animationTime = 0;
    }

    // Centralized collision engine: Subclasses inherit these for free!
    protected boolean moveX(float amount, List<Rectangle> solids) {
        if (amount == 0)
            return false;

        position.x += amount;
        Rectangle enemyBounds = getBounds();
        boolean collisionDetected = false;

        for (Rectangle solid : solids) {
            if (enemyBounds.overlaps(solid)) {
                collisionDetected = true;
                if (amount > 0) {
                    position.x = solid.x - enemyBounds.width;
                } else {
                    position.x = solid.x + solid.width;
                }
                velocity.x = 0;
                break;
            }
        }
        return collisionDetected;
    }

    protected void moveY(float amount, List<Rectangle> solids) {
        if (amount == 0)
            return;

        position.y += amount;
        Rectangle enemyBounds = getBounds();

        for (Rectangle solid : solids) {
            if (!enemyBounds.overlaps(solid))
                continue;

            if (amount < 0) { // Landing
                position.y = solid.y + solid.height;
                velocity.y = 0;
                isOnGround = true;
            } else { // Ceiling hit
                position.y = solid.y - enemyBounds.height;
                velocity.y = 0;
            }
            return;
        }
    }
}
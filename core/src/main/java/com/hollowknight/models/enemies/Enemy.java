package com.hollowknight.models.enemies;

import java.util.List;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;
import com.hollowknight.views.GameAssetManager;

public abstract class Enemy {
    public final Vector2 respawnPosition;
    public Vector2 position;
    public Vector2 velocity;

    public EnemyAnimations animation;
    public float animationTime = 0;

    public int facingDirection = Constants.RIGHT_DIRECTION;
    public boolean isOnGround = false;
    public boolean isDead = false;

    protected float knockbackTimer = 0f;

    protected int hp;

    private boolean isJustDead = false;

    public EnemyType type;

    public Enemy(Vector2 pos) {
        this.position = new Vector2(pos);
        this.respawnPosition = new Vector2(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
    }

    // Every unique enemy must implement these methods
    public abstract void update(float delta, Player player, List<Rectangle> solidBlocks);

    public abstract Rectangle getBounds();

    public void kill() {
        this.hp = 0;
        this.isDead = true;
        this.velocity.set(0, 0);
        isJustDead = true;
    }

    public boolean hasUnregisteredDeath() {
        return isJustDead;
    }

    public void registerDeath() {
        isJustDead = false;
    }

    public void takeDamage(int damage, float sourceX, boolean knockback, float knockbackMultiplier) {
        this.hp -= damage;
        AudioController.getInstance().playSfx(GameAssetManager.enemyHurtSfx);
        if (hp <= 0) {
            this.kill();
        }

        if (knockback) {
            this.knockbackTimer = Constants.ENEMY_KNOCKBACK_DURATION;
            float knockbackDir = (this.position.x < sourceX) ? -1f : 1f;
            this.velocity.x = Constants.ENEMY_KNOCKBACK_SPEED_X * knockbackDir * knockbackMultiplier;
            this.velocity.y = Constants.ENEMY_KNOCKBACK_SPEED_Y * knockbackMultiplier;
            this.isOnGround = false;
        }
    }

    public int getHp() {
        return this.hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getCollisionDamage() {
        return 1;
    }

    public void respawn() {
        this.position.set(respawnPosition);
        this.velocity.set(0, Constants.GRAVITY);
        this.facingDirection = Constants.RIGHT_DIRECTION;
        this.isOnGround = false;
        this.isDead = false;
        this.animationTime = 0;
        this.knockbackTimer = 0;
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
package com.hollowknight.models.enemies;

import java.util.List;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class GroundEnemy extends Enemy {

    // --- Properties & State ---
    public GroundEnemyType type;
    private boolean isTurning = false;
    private float turnTimer = 0f;

    // --- Initialization & Lifecycle ---
    private GroundEnemy(GroundEnemyType type, Vector2 pos) {
        super(pos);
        this.type = type;
        this.velocity = new Vector2(type.speed * facingDirection, Constants.GRAVITY);
        this.hp = Constants.GROUND_ENEMY_HP;
        setAnimation(type.walkAnimation);
    }

    public static GroundEnemy newEnemy(GroundEnemyType type, Vector2 pos) {
        return new GroundEnemy(type, pos);
    }

    @Override
    public void respawn() {
        this.position = respawnPosition;
        this.velocity = new Vector2(type.speed * facingDirection, Constants.GRAVITY);
        this.facingDirection = Constants.RIGHT_DIRECTION;
        this.isOnGround = false;
        this.isDead = false;
        this.isTurning = false;
        this.turnTimer = 0f;
        setAnimation(type.walkAnimation);
    }

    @Override
    public void kill() {
        super.kill();
        isDead = true;
        velocity.set(0, 0);
        setAnimation(type.deathAnimation);
    }

    // --- Core Update Loop ---
    @Override
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        animationTime += delta;

        if (knockbackTimer > 0) {
            knockbackTimer -= delta;
            velocity.y += Constants.GRAVITY * delta;
            isOnGround = false;
            moveX(velocity.x * delta, solidBlocks);
            moveY(velocity.y * delta, solidBlocks);

            if (knockbackTimer <= 0) {
                velocity.x = 0;
            }
            return;
        }

        if (isDead) {
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta;
            moveY(velocity.y * delta, solidBlocks);
            return;
        }

        if (isTurning) {
            turnTimer -= delta;
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta;

            updatePosition(delta, solidBlocks);

            if (turnTimer <= 0) {
                isTurning = false;
                facingDirection *= -1;
                setAnimation(type.walkAnimation);
            }
            return;
        }

        velocity.x = type.speed * facingDirection;
        velocity.y += Constants.GRAVITY * delta;

        updatePosition(delta, solidBlocks);
    }

    // --- Movement & Physics ---
    private void updatePosition(float delta, List<Rectangle> solids) {
        isOnGround = false;

        boolean hitWall = moveX(velocity.x * delta, solids);
        moveY(velocity.y * delta, solids);

        if ((hitWall || isEdgeAhead(solids)) && !isTurning && isOnGround) {
            startTurn();
        }
    }

    private boolean isEdgeAhead(List<Rectangle> solids) {
        if (!isOnGround)
            return false;

        float width = getBounds().width;
        float checkX = position.x + (facingDirection == 1 ? width + 5 : -5);
        float checkY = position.y - 5;

        for (Rectangle solid : solids) {
            if (solid.contains(checkX, checkY)) {
                return false;
            }
        }
        return true;
    }

    private void startTurn() {
        isTurning = true;
        turnTimer = Constants.GROUND_ENEMY_TURN_TIMER;
        setAnimation(type.turnAnimation);
    }

    // --- Animation & Properties ---
    private void setAnimation(EnemyAnimations newState) {
        if (animation != newState) {
            animation = newState;
            animationTime = 0;
        }
    }

    @Override
    public Rectangle getBounds() {
        return type.getHitbox(position.x, position.y);
    }
}
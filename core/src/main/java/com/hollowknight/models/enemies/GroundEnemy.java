package com.hollowknight.models.enemies;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class GroundEnemy extends Enemy {
    public GroundEnemyType type;

    // Turning logic variables
    private boolean isTurning = false;
    private float turnTimer = 0f;

    private GroundEnemy(GroundEnemyType type, Vector2 pos) {
        super(pos);
        this.type = type;
        this.velocity = new Vector2(type.speed * facingDirection, Constants.GRAVITY);
        setAnimation(type.walkAnimation);
    }

    public static GroundEnemy newEnemy(GroundEnemyType type, Vector2 pos) {
        return new GroundEnemy(type, pos);
    }

    public void respawn() {
        this.position = respawnPosition;
        this.velocity = new Vector2(type.speed * facingDirection, Constants.GRAVITY);
        setAnimation(type.walkAnimation);

        facingDirection = Constants.RIGHT_DIRECTION;
        isOnGround = false;
        isDead = false;

        // Turning logic variables
        isTurning = false;
        turnTimer = 0f;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        // --- 1. HANDLE TIMERS ---
        animationTime += delta;

        if (isDead)
            return;

        // --- 2. HANDLE TURNING STATE ---
        if (isTurning) {
            turnTimer -= delta;
            velocity.x = 0; // Stop moving horizontally while turning

            // Wait for gravity while turning
            velocity.y += Constants.GRAVITY * delta;
            updatePosition(delta, solidBlocks);

            if (turnTimer <= 0) {
                isTurning = false;
                facingDirection *= -1; // Physically flip direction
                setAnimation(type.walkAnimation); // Resume walking
            }
            return; // Exit early to prevent normal movement logic
        }

        // --- 3. CALCULATE VELOCITY (Movement & Gravity) ---
        velocity.x = type.speed * facingDirection;
        velocity.y += Constants.GRAVITY * delta;

        // --- 4. RESOLVE MOVEMENT & COLLISIONS ---
        updatePosition(delta, solidBlocks);
    }

    private void updatePosition(float delta, List<Rectangle> solids) {
        isOnGround = false;

        boolean hitWall = moveX(velocity.x * delta, solids);
        moveY(velocity.y * delta, solids);

        // --- 5. UPDATE ENEMY STATES ---
        if (hitWall && !isTurning && isOnGround) {
            startTurn();
        }
    }

    private void startTurn() {
        isTurning = true;
        turnTimer = Constants.CRAWLID_TURN_TIMER;
        setAnimation(type.turnAnimation);
    }

    public void kill() {
        isDead = true;
        velocity.set(0, 0); // Stop all momentum
        setAnimation(type.deathAnimation);
    }

    // --- Centralized Animation Chooser ---
    private void setAnimation(EnemyAnimations newState) {
        if (animation != newState) {
            animation = newState;
            animationTime = 0; // Reset timer for new animation
        }
    }

    @Override
    public Rectangle getBounds() {
        return type.getHitbox(position.x, position.y);
    }
}
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
        this.hp = Constants.GROUND_ENEMY_HP;
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

        if (knockbackTimer > 0) {
            knockbackTimer -= delta;
            velocity.y += Constants.GRAVITY * delta;

            isOnGround = false;
            moveX(velocity.x * delta, solidBlocks);
            moveY(velocity.y * delta, solidBlocks);

            if (knockbackTimer <= 0) {
                velocity.x = 0;
            }
            return; // Intercept and bypass normal AI logic while flying backward
        }

        if (isDead) {
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta;
            moveY(velocity.y * delta, solidBlocks);
            return; // Allow the dead body to stay pinned to the ground via gravity
        }

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
        if ((hitWall || isEdgeAhead(solids)) && !isTurning && isOnGround) {
            startTurn();
        }
    }

    private boolean isEdgeAhead(List<Rectangle> solids) {
        // Only check for edges if we are actually standing on the ground
        if (!isOnGround)
            return false;

        // Project a point slightly ahead of the enemy and slightly below the platform
        // floor
        float width = getBounds().width;
        float checkX = position.x + (facingDirection == 1 ? width + 5 : -5);
        float checkY = position.y - 5;

        for (Rectangle solid : solids) {
            if (solid.contains(checkX, checkY)) {
                return false; // Found solid ground ahead, it is NOT an edge
            }
        }
        return true; // No ground found ahead, it's an edge!
    }

    private void startTurn() {
        isTurning = true;
        turnTimer = Constants.GROUND_ENEMY_TURN_TIMER;
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
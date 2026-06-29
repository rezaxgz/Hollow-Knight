package com.hollowknight.models.player.enemies;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;

public class GroundEnemy {
    public final Vector2 respawnPosition;

    public Vector2 position = new Vector2();
    public Vector2 velocity;

    public GroundEnemyAnimations animation;
    public float animationTime = 0;

    public GroundEnemyType type;

    // State variables (Mirrors player status/movement states)
    public int facingDirection = Constants.RIGHT_DIRECTION;
    public boolean isOnGround = false;
    public boolean isDead = false;

    // Turning logic variables
    private boolean isTurning = false;
    private float turnTimer = 0f;

    private GroundEnemy(GroundEnemyType type, Vector2 pos) {
        this.type = type;
        this.position = pos;
        this.respawnPosition = pos;
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

    public void update(float delta, List<Rectangle> solidBlocks) {
        if (isDead)
            return;

        // --- 1. HANDLE TIMERS ---
        animationTime += delta;

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
    private void setAnimation(GroundEnemyAnimations newState) {
        if (animation != newState) {
            animation = newState;
            animationTime = 0; // Reset timer for new animation
        }
    }

    public Rectangle getBounds() {
        return type.getHitbox(position.x, position.y);
    }

    // --- Axis-Separated Collision Resolution (Mirrors Player.java) ---
    private boolean moveX(float amount, List<Rectangle> solids) {
        if (amount == 0)
            return false;

        position.x += amount;
        Rectangle enemyBounds = getBounds();
        boolean collisionDetected = false;

        for (Rectangle solid : solids) {
            if (enemyBounds.overlaps(solid)) {
                collisionDetected = true;

                if (amount > 0) { // Hitting wall on the right
                    position.x = solid.x - enemyBounds.width;
                } else { // Hitting wall on the left
                    position.x = solid.x + solid.width;
                }

                velocity.x = 0;
                break;
            }
        }
        return collisionDetected;
    }

    private void moveY(float amount, List<Rectangle> solids) {
        if (amount == 0)
            return;

        position.y += amount;
        Rectangle enemyBounds = getBounds();

        for (Rectangle solid : solids) {
            if (!enemyBounds.overlaps(solid))
                continue;

            if (amount < 0) { // Landing on the ground
                position.y = solid.y + solid.height;
                velocity.y = 0;
                isOnGround = true;
            } else { // Hitting a ceiling
                position.y = solid.y - enemyBounds.height;
                velocity.y = 0;
            }
            return;
        }
    }
}
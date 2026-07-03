package com.hollowknight.models.enemies;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class CrystalGuardian extends Enemy {
    public final static EnemyAnimations IDLE_ANIMATION = EnemyAnimations.CRYSTALLIZED_IDLE;
    public final static EnemyAnimations SHOOT_ANIMATION = EnemyAnimations.CRYSTALLIZED_SHOOT;
    public final static EnemyAnimations RUN_ANIMATION = EnemyAnimations.CRYSTALLIZED_RUN;
    public final static EnemyAnimations TURN_ANIMATION = EnemyAnimations.CRYSTALLIZED_TURN;
    public final static EnemyAnimations DEATH_ANIMATION = EnemyAnimations.CRYSTALLIZED_DEATH;

    // --- ENEMY STATES ---
    public enum State {
        IDLE, SHOOT, FIRE, RUN, TURN, DEAD
    }

    public State currentState = State.IDLE;

    // Timers & Variables
    private float stateTimer = 0f;
    private float runTimer = 0f;

    private static final float AGGRO_RANGE = 600f; // Sight distance
    private static final float RUN_DURATION = 3.0f; // How long he runs before stopping
    private static final float LASER_DURATION = 1.0f; // How long the laser stays active AFTER the animation
    private static final float LASER_HEIGHT = 32f;
    private static final float LASER_MAX_RANGE = 2000f;
    private static final float LASER_X_OFFSET = 15f;

    private CrystalGuardian(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        changeState(State.IDLE);
        this.hp = Constants.GUARDIAN_HP;
        this.facingDirection = Constants.LEFT_DIRECTION;
    }

    public static CrystalGuardian newEnemy(Vector2 pos) {
        return new CrystalGuardian(pos);
    }

    @Override
    public void respawn() {
        this.position = new Vector2(respawnPosition);
        this.velocity = new Vector2(0, Constants.GRAVITY);

        facingDirection = Constants.LEFT_DIRECTION;
        isOnGround = false;
        isDead = false;

        changeState(State.IDLE);

        this.hp = Constants.GUARDIAN_HP;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        animationTime += delta;

        // 1. Handle Knockback
        if (knockbackTimer > 0) {
            knockbackTimer -= delta;
            velocity.y += Constants.GRAVITY * delta;
            isOnGround = false;
            moveX(velocity.x * delta, solidBlocks);
            moveY(velocity.y * delta, solidBlocks);
            if (knockbackTimer <= 0)
                velocity.x = 0;
            return;
        }

        // 2. Handle Death
        if (isDead) {
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta;
            moveY(velocity.y * delta, solidBlocks);
            return;
        }

        // Apply gravity for active states
        velocity.y += Constants.GRAVITY * delta;

        // 3. State Machine
        switch (currentState) {
            case IDLE:
                velocity.x = 0;
                // Watch for player
                if (canSeePlayer(player)) {
                    changeState(State.SHOOT);
                    facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION
                            : Constants.LEFT_DIRECTION;
                }
                break;

            case SHOOT:
                velocity.x = 0;
                stateTimer += delta;

                // Wait for the "charge up" animation to finish
                if (stateTimer >= SHOOT_ANIMATION.totalDuration) {
                    // Lock onto the player's direction before firing
                    if (player.position.x < this.position.x) {
                        facingDirection = Constants.LEFT_DIRECTION;
                    } else {
                        facingDirection = Constants.RIGHT_DIRECTION;
                    }
                    changeState(State.FIRE);
                }
                break;

            case FIRE:
                velocity.x = 0;
                stateTimer += delta;

                // The laser is now active! Hold it for LASER_DURATION
                if (stateTimer >= LASER_DURATION) {
                    changeState(State.RUN);
                }
                break;

            case RUN:
                stateTimer += delta;
                runTimer += delta;

                // When run timer runs out, go back to idle
                if (runTimer >= RUN_DURATION) {
                    changeState(State.IDLE);
                    runTimer = 0;
                    break;
                }

                velocity.x = Constants.GUARDIAN_SPEED * facingDirection;
                boolean hitWall = moveX(velocity.x * delta, solidBlocks);

                // Turn if he hits a solid block
                if (hitWall) {
                    changeState(State.TURN);
                }
                break;

            case TURN:
                velocity.x = 0;
                stateTimer += delta;
                // Wait for turn animation to finish
                if (stateTimer >= TURN_ANIMATION.totalDuration) {
                    facingDirection *= -1; // Flip direction
                    changeState(State.RUN);
                }
                break;

            case DEAD:
                velocity.x = 0;
                break;
        }

        // Vertical collision
        moveY(velocity.y * delta, solidBlocks);
    }

    // --- State & Animation Manager ---
    private void changeState(State newState) {
        currentState = newState;
        stateTimer = 0f;

        switch (newState) {
            case IDLE:
                setAnimation(IDLE_ANIMATION);
                break;
            case SHOOT:
                setAnimation(SHOOT_ANIMATION);
                break;
            case FIRE:
                // We don't set a new animation here; because SHOOT is ONESHOT,
                // it will naturally hold on its final frame while firing the laser.
                break;
            case RUN:
                runTimer = 0f;
                setAnimation(RUN_ANIMATION);
                break;
            case TURN:
                setAnimation(TURN_ANIMATION);
                break;
            case DEAD:
                setAnimation(DEATH_ANIMATION);
                break;
        }
    }

    private void setAnimation(EnemyAnimations newState) {
        if (animation != newState) {
            animation = newState;
            animationTime = 0;
        }
    }

    @Override
    public void kill() {
        isDead = true;
        velocity.set(0, 0);
        changeState(State.DEAD);
    }

    // --- Helper Logic ---
    private boolean canSeePlayer(Player player) {
        if (player.isDead())
            return false;

        // Check roughly same height
        boolean sameHeight = Math.abs(player.position.y - this.position.y) < Constants.GUARDIAN_HITBOX_HEIGHT;
        if (!sameHeight)
            return false;

        float distance = player.position.x - this.position.x;
        if (facingDirection == Constants.RIGHT_DIRECTION && distance > 0 && distance < AGGRO_RANGE) {
            return true;
        } else if (facingDirection == Constants.LEFT_DIRECTION && distance < 0 && distance > -AGGRO_RANGE) {
            return true;
        }
        return false;
    }

    private float getLaserStartX() {
        return (facingDirection == Constants.RIGHT_DIRECTION)
                ? position.x + Constants.GUARDIAN_HITBOX_WIDTH - LASER_X_OFFSET
                : position.x + LASER_X_OFFSET;
    }

    public float getLaserCircleStartX() {
        return (facingDirection == Constants.RIGHT_DIRECTION)
                ? getLaserStartX() - 12
                : getLaserStartX() - 24;
    }

    private float getLaserStartY() {
        return position.y + (Constants.GUARDIAN_HITBOX_HEIGHT / 2f) - (LASER_HEIGHT / 2f) + 5;
    }

    public float getLaserCircleStartY() {
        return getLaserStartY() - 8;
    }

    public int getLaserAnimationIndex() {
        float period = 0.25f;
        float x = stateTimer % period;
        if (x < period / 4f) {
            return 0;
        }
        if (x < period / 2f) {
            return 1;
        }
        if (x < 3f * period / 4f) {
            return 2;
        }
        return 3;
    }

    /**
     * Calculates how far the laser travels before hitting a wall.
     * GameWorld will call this to check if the player overlaps it.
     */
    public Rectangle getActiveLaserBounds(List<Rectangle> solidBlocks) {
        // Laser ONLY exists during the FIRE state
        if (currentState != State.FIRE || isDead) {
            return null;
        }

        float startX = getLaserStartX();
        float startY = getLaserStartY();

        float endX = startX + (facingDirection * LASER_MAX_RANGE);

        // Raycast logic: shorten the laser if it hits a solid block
        for (Rectangle solid : solidBlocks) {
            // Check if solid intersects the vertical space of the laser
            if (solid.y < startY + LASER_HEIGHT && solid.y + solid.height > startY) {
                if (facingDirection == Constants.RIGHT_DIRECTION) {
                    if (solid.x > startX && solid.x < endX) {
                        endX = solid.x; // Hit wall, stop laser here
                    }
                } else { // Facing Left
                    if (solid.x + solid.width < startX && solid.x + solid.width > endX) {
                        endX = solid.x + solid.width; // Hit wall, stop laser here
                    }
                }
            }
        }

        float finalX = (facingDirection == Constants.RIGHT_DIRECTION) ? startX : endX;
        float width = Math.abs(endX - startX);

        return new Rectangle(finalX, startY, width, LASER_HEIGHT);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y,
                Constants.GUARDIAN_HITBOX_WIDTH, Constants.GUARDIAN_HITBOX_HEIGHT);
    }

    @Override
    public int getCollisionDamage() {
        return 3;
    }
}
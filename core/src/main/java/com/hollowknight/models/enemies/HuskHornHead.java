package com.hollowknight.models.enemies;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player; // Assuming this import path based on your package

public class HuskHornHead extends Enemy {
    public final static EnemyAnimations WALK_ANIMATION = EnemyAnimations.HORNHEAD_WALK;
    public final static EnemyAnimations IDLE_ANIMATION = EnemyAnimations.HORNHEAD_IDLE;
    public final static EnemyAnimations ATTACK_ANIMATION = EnemyAnimations.HORNHEAD_ATTACK_RUN;
    public final static EnemyAnimations DEATH_ANIMATION = EnemyAnimations.HORNHEAD_DEATH;
    public final static EnemyAnimations ATTACK_START_ANIMATION = EnemyAnimations.HORNHEAD_ATTACK_START;
    public final static EnemyAnimations TURN_ANIMATION = EnemyAnimations.HORNHEAD_TURN;

    // --- ENEMY STATES ---
    public enum State {
        WALK, REST, TURN, ATTACK_START, ATTACK_CHARGE, DEAD
    }

    public State currentState = State.WALK;

    // Timers
    private float stateTimer = 0f;
    private float walkTimer = 0f;

    private static final float SIGHT_RANGE = 700f;
    private static final float SIGHT_HEIGHT_TOLERANCE = Constants.HORNHEAD_HITBOX_HEIGHT - 20;

    private HuskHornHead(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        changeState(State.WALK);
        this.hp = Constants.HORNHEAD_HP;
    }

    public static HuskHornHead newEnemy(Vector2 pos) {
        return new HuskHornHead(pos);
    }

    public void respawn() {
        this.position = new Vector2(respawnPosition);
        this.velocity = new Vector2(0, Constants.GRAVITY);

        facingDirection = Constants.RIGHT_DIRECTION;
        isOnGround = false;
        isDead = false;

        changeState(State.WALK);

        this.hp = Constants.HORNHEAD_HP;
    }

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
            return; // Intercept and bypass normal AI logic while flying backward
        }

        if (isDead) {
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta;
            moveY(velocity.y * delta, solidBlocks);
            return; // Allow the dead body to stay pinned to the ground via gravity
        }

        // Apply gravity
        velocity.y += Constants.GRAVITY * delta;

        // Handle logic based on current state
        switch (currentState) {
            case WALK:
                stateTimer += delta;
                walkTimer += delta;

                // 1. Check for player sight
                if (canSeePlayer(player, solidBlocks)) {
                    changeState(State.ATTACK_START);
                    break;
                }

                // 2. Check if tired
                if (walkTimer >= Constants.HORNHEAD_WALK_TIMER) {
                    changeState(State.REST);
                    velocity.x = 0;
                    break;
                }

                // 3. Move and check for walls/edges
                velocity.x = Constants.HORNHEAD_SPEED * facingDirection;
                boolean hitWall = moveX(velocity.x * delta, solidBlocks);

                if (hitWall || isEdgeAhead(solidBlocks)) {
                    changeState(State.TURN);
                }
                break;

            case REST:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= Constants.HORNHEAD_REST_TIMER) {
                    changeState(State.WALK);
                }
                break;

            case TURN:
                velocity.x = 0;
                stateTimer += delta;
                walkTimer += delta;
                if (stateTimer >= Constants.HORNHEAD_TURN_TIMER) {
                    facingDirection *= -1; // Flip direction
                    changeState(State.WALK);
                }
                break;

            case ATTACK_START:
                velocity.x = 0;
                stateTimer += delta;
                // Wait for the anticipation animation to finish
                if (stateTimer >= ATTACK_START_ANIMATION.totalDuration) {
                    changeState(State.ATTACK_CHARGE);
                }
                break;

            case ATTACK_CHARGE:
                // Run fast! No edge detection here, so he will fall if he runs off.
                velocity.x = Constants.HORNHEAD_ATTACK_SPEED * facingDirection;
                boolean hitWallCharge = moveX(velocity.x * delta, solidBlocks);

                if (hitWallCharge) {
                    changeState(State.TURN); // Hit a wall, go back to normal logic
                }
                break;

            case DEAD:
                velocity.x = 0;
                break;
        }

        // Apply vertical movement and collision
        moveY(velocity.y * delta, solidBlocks);
    }

    // --- State & Animation Manager ---
    private void changeState(State newState) {
        currentState = newState;
        stateTimer = 0f;

        switch (newState) {
            case WALK:
                setAnimation(WALK_ANIMATION);
                break;
            case REST:
                setAnimation(IDLE_ANIMATION);
                walkTimer = 0;
                break;
            case TURN:
                setAnimation(TURN_ANIMATION);
                break;
            case ATTACK_START:
                walkTimer = 0;
                setAnimation(ATTACK_START_ANIMATION);
                break;
            case ATTACK_CHARGE:
                walkTimer = 0;
                setAnimation(ATTACK_ANIMATION);
                break;
            case DEAD:
                walkTimer = 0;
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
        super.kill();
        isDead = true;
        velocity.set(0, 0);
        changeState(State.DEAD);
    }

    // --- Helper Logic ---
    private boolean isEdgeAhead(List<Rectangle> solids) {
        // Only check for edges if we are actually on the ground
        if (!isOnGround)
            return false;

        // Project a point slightly ahead of the enemy and slightly below the floor
        float checkX = position.x + (facingDirection == 1 ? Constants.HORNHEAD_HITBOX_WIDTH + 5 : -5);
        float checkY = position.y - 5;

        for (Rectangle solid : solids) {
            if (solid.contains(checkX, checkY)) {
                return false; // Found ground ahead, not an edge
            }
        }
        return true; // No ground found, it's an edge
    }

    private boolean canSeePlayer(Player player, List<Rectangle> solidBlocks) {
        if (player.isDead())
            return false;

        // Check if player is roughly on the same vertical level
        boolean sameHeight = Math.abs(player.position.y - this.position.y) < SIGHT_HEIGHT_TOLERANCE;
        if (!sameHeight)
            return false;

        // Check horizontal distance and direction
        float distance = player.position.x - this.position.x;

        boolean inRangeAndFacing = false;
        if (facingDirection == Constants.RIGHT_DIRECTION && distance > 0 && distance < SIGHT_RANGE) {
            inRangeAndFacing = true;
        } else if (facingDirection == Constants.LEFT_DIRECTION && distance < 0 && distance > -SIGHT_RANGE) {
            inRangeAndFacing = true;
        }

        // If not facing the right way or out of range, fail early
        if (!inRangeAndFacing) {
            return false;
        }

        // --- LINE OF SIGHT CHECK ---
        float startX = Math.min(this.position.x, player.position.x);
        float endX = Math.max(this.position.x, player.position.x);

        // Cast the sight line from roughly the center/eye-level of the enemy to avoid
        // floor clipping
        float sightY = this.position.y + (Constants.HORNHEAD_HITBOX_HEIGHT / 2f);

        for (Rectangle solid : solidBlocks) {
            // Check if the solid block is horizontally between the enemy and the player
            if (solid.x + solid.width > startX && solid.x < endX) {
                // Check if the solid block intersects the sight line vertically
                if (sightY >= solid.y && sightY <= solid.y + solid.height) {
                    return false; // Vision is blocked by a wall
                }
            }
        }

        return true;
    }

    @Override
    public Rectangle getBounds() {
        if (currentState == State.ATTACK_CHARGE || currentState == State.ATTACK_START) {
            return new Rectangle(position.x, position.y, Constants.HORNHEAD_HITBOX_WIDTH,
                    Constants.HORNHEAD_HITBOX_HEIGHT_ATTACKING);
        }
        return new Rectangle(position.x, position.y, Constants.HORNHEAD_HITBOX_WIDTH,
                Constants.HORNHEAD_HITBOX_HEIGHT);
    }

    @Override
    public int getCollisionDamage() {
        return 3;
    }

    @Override
    public void takeDamage(int damage, float sourceX, boolean knockback, float knockbackMultiplier) {
        super.takeDamage(damage, sourceX, knockback, knockbackMultiplier);
        // apply less knock back because hornhead is heavy
        if (knockback) {
            this.velocity.x -= 100;
            this.velocity.y -= 20;
        }
    }
}
package com.hollowknight.models.enemies;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class Mossfly extends Enemy {
    public final static EnemyAnimations SHAKE_ANIMATION = EnemyAnimations.MOSSFLY_SHAKE;
    public final static EnemyAnimations APPEAR_ANIMATION = EnemyAnimations.MOSSFLY_APPEAR;
    public final static EnemyAnimations FLY_ANIMATION = EnemyAnimations.MOSSFLY_FLY;
    public final static EnemyAnimations DEATH_ANIMATION = EnemyAnimations.MOSSFLY_DEATH;

    // --- ENEMY STATES ---
    public enum State {
        SHAKE, APPEAR, FLY, DEAD
    }

    public State currentState = State.SHAKE;

    // Timers & Range
    private float stateTimer = 0f;
    private static final float AGGRO_RANGE = 350f;

    private Mossfly(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        changeState(State.SHAKE);
        this.hp = Constants.MOSSFLY_HP; // Make sure to add MOSSFLY_HP to your Constants
    }

    public static Mossfly newEnemy(Vector2 pos) {
        return new Mossfly(pos);
    }

    public void respawn() {
        this.position = new Vector2(respawnPosition);
        this.velocity = new Vector2(0, Constants.GRAVITY);

        facingDirection = Constants.RIGHT_DIRECTION;
        isOnGround = false;
        isDead = false;

        changeState(State.SHAKE);

        this.hp = Constants.MOSSFLY_HP;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        animationTime += delta;

        // 1. Handle Knockback (Takes priority over normal movement)
        if (knockbackTimer > 0) {
            knockbackTimer -= delta;
            velocity.y += Constants.GRAVITY * delta; // Gravity still applies during knockback

            isOnGround = false;
            moveX(velocity.x * delta, solidBlocks);
            moveY(velocity.y * delta, solidBlocks);

            if (knockbackTimer <= 0) {
                velocity.x = 0;
            }
            return;
        }

        // 2. Handle Dead State (Falls to the ground)
        if (isDead) {
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta; // Gravity pulls the dead body down
            moveY(velocity.y * delta, solidBlocks);
            return;
        }

        // 3. Handle Active States
        switch (currentState) {
            case SHAKE:
                // Apply gravity to keep him on the floor while resting
                velocity.x = 0;
                velocity.y += Constants.GRAVITY * delta;

                // Check distance to player
                if (getDistanceToPlayer(player) <= AGGRO_RANGE && !player.isDead()) {
                    changeState(State.APPEAR);
                }
                break;

            case APPEAR:
                stateTimer += delta;
                // he lifts off
                velocity.set(0, 100 * stateTimer);

                // Wait for the appear animation to finish before flying
                if (stateTimer >= APPEAR_ANIMATION.totalDuration) {
                    changeState(State.FLY);
                }
                break;

            case FLY:
                // No gravity while flying, strictly move towards the player
                if (!player.isDead()) {
                    // Calculate direction vector towards the player
                    Vector2 direction = new Vector2(player.position.x - this.position.x,
                            player.position.y - this.position.y);
                    direction.nor(); // Normalize to get a pure direction vector

                    // Set velocity based on speed and direction
                    velocity.x = direction.x * Constants.MOSSFLY_SPEED; // Add MOSSFLY_SPEED to Constants
                    velocity.y = direction.y * Constants.MOSSFLY_SPEED;

                    // If moving downwards (negative Y velocity), reduce the downward speed
                    // significantly
                    // so it prefers to stay in the air rather than skimming the ground
                    if (velocity.y < 0) {
                        velocity.y *= 0.1f;
                    }

                    // Update facing direction based on movement
                    if (velocity.x > 0) {
                        facingDirection = Constants.RIGHT_DIRECTION;
                    } else if (velocity.x < 0) {
                        facingDirection = Constants.LEFT_DIRECTION;
                    }
                } else {
                    // If player is dead, hover in place or fly away
                    velocity.set(0, 0);
                }
                break;

            case DEAD:
                // Handled at the top of the update method
                break;
        }

        // 4. Resolve Movement and Collisions
        moveX(velocity.x * delta, solidBlocks);
        moveY(velocity.y * delta, solidBlocks);
    }

    // --- State & Animation Manager ---
    private void changeState(State newState) {
        currentState = newState;
        stateTimer = 0f;

        switch (newState) {
            case SHAKE:
                setAnimation(SHAKE_ANIMATION);
                break;
            case APPEAR:
                setAnimation(APPEAR_ANIMATION);
                break;
            case FLY:
                setAnimation(FLY_ANIMATION);
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
        super.kill();
        isDead = true;
        velocity.set(0, 0); // Stop momentum before gravity takes over
        changeState(State.DEAD);
    }

    // --- Helper Logic ---
    private float getDistanceToPlayer(Player player) {
        return position.dst(player.position);
    }

    @Override
    public Rectangle getBounds() {
        // Adjust these constants to match your desired Mossfly hitbox size
        return new Rectangle(position.x, position.y,
                Constants.MOSSFLY_HITBOX_WIDTH,
                Constants.MOSSFLY_HITBOX_HEIGHT);
    }

    @Override
    public int getCollisionDamage() {
        return 1;
    }
}
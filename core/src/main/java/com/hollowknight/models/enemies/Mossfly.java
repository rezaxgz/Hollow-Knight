package com.hollowknight.models.enemies;

import java.util.List;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class Mossfly extends Enemy {

    // --- Constants & Enums ---
    public final static EnemyAnimations SHAKE_ANIMATION = EnemyAnimations.MOSSFLY_SHAKE;
    public final static EnemyAnimations APPEAR_ANIMATION = EnemyAnimations.MOSSFLY_APPEAR;
    public final static EnemyAnimations FLY_ANIMATION = EnemyAnimations.MOSSFLY_FLY;
    public final static EnemyAnimations DEATH_ANIMATION = EnemyAnimations.MOSSFLY_DEATH;

    public enum State {
        SHAKE, APPEAR, FLY, DEAD
    }

    private static final float AGGRO_RANGE = 350f;

    // --- State Properties ---
    public State currentState = State.SHAKE;
    private float stateTimer = 0f;

    // --- Initialization & Lifecycle ---
    private Mossfly(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        this.hp = Constants.MOSSFLY_HP;
        changeState(State.SHAKE);
    }

    public static Mossfly newEnemy(Vector2 pos) {
        return new Mossfly(pos);
    }

    @Override
    public void respawn() {
        this.position = new Vector2(respawnPosition);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        this.facingDirection = Constants.RIGHT_DIRECTION;
        this.isOnGround = false;
        this.isDead = false;
        this.hp = Constants.MOSSFLY_HP;
        changeState(State.SHAKE);
    }

    @Override
    public void kill() {
        super.kill();
        isDead = true;
        velocity.set(0, 0);
        changeState(State.DEAD);
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

        switch (currentState) {
            case SHAKE:
                velocity.x = 0;
                velocity.y += Constants.GRAVITY * delta;

                if (getDistanceToPlayer(player) <= AGGRO_RANGE && !player.isDead()) {
                    changeState(State.APPEAR);
                }
                break;

            case APPEAR:
                stateTimer += delta;
                velocity.set(0, 100 * stateTimer);

                if (stateTimer >= APPEAR_ANIMATION.totalDuration) {
                    changeState(State.FLY);
                }
                break;

            case FLY:
                if (!player.isDead()) {
                    Vector2 direction = new Vector2(player.position.x - this.position.x,
                            player.position.y - this.position.y);
                    direction.nor();

                    velocity.x = direction.x * Constants.MOSSFLY_SPEED;
                    velocity.y = direction.y * Constants.MOSSFLY_SPEED;

                    if (velocity.y < 0) {
                        velocity.y *= 0.1f;
                    }

                    if (velocity.x > 0) {
                        facingDirection = Constants.RIGHT_DIRECTION;
                    } else if (velocity.x < 0) {
                        facingDirection = Constants.LEFT_DIRECTION;
                    }
                } else {
                    velocity.set(0, 0);
                }
                break;

            case DEAD:
                break;
        }

        moveX(velocity.x * delta, solidBlocks);
        moveY(velocity.y * delta, solidBlocks);
    }

    // --- State & Animation Management ---
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

    // --- AI & Properties ---
    private float getDistanceToPlayer(Player player) {
        return position.dst(player.position);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y,
                Constants.MOSSFLY_HITBOX_WIDTH,
                Constants.MOSSFLY_HITBOX_HEIGHT);
    }

    @Override
    public int getCollisionDamage() {
        return 1;
    }
}
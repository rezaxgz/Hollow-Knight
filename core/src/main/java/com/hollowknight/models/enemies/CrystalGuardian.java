package com.hollowknight.models.enemies;

import java.util.List;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class CrystalGuardian extends Enemy {

    // --- Constants & Enums ---
    public final static EnemyAnimations IDLE_ANIMATION = EnemyAnimations.CRYSTALLIZED_IDLE;
    public final static EnemyAnimations SHOOT_ANIMATION = EnemyAnimations.CRYSTALLIZED_SHOOT;
    public final static EnemyAnimations RUN_ANIMATION = EnemyAnimations.CRYSTALLIZED_RUN;
    public final static EnemyAnimations TURN_ANIMATION = EnemyAnimations.CRYSTALLIZED_TURN;
    public final static EnemyAnimations DEATH_ANIMATION = EnemyAnimations.CRYSTALLIZED_DEATH;

    public enum State {
        IDLE, SHOOT, FIRE, RUN, TURN, DEAD
    }

    private static final float AGGRO_RANGE = 600f;
    private static final float RUN_DURATION = 3.0f;
    private static final float LASER_DURATION = 1.0f;
    private static final float LASER_HEIGHT = 32f;
    private static final float LASER_MAX_RANGE = 2000f;
    private static final float LASER_X_OFFSET = 15f;

    // --- State Properties ---
    public State currentState = State.IDLE;
    private float stateTimer = 0f;
    private float runTimer = 0f;

    // --- Initialization & Lifecycle ---
    private CrystalGuardian(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        this.hp = Constants.GUARDIAN_HP;
        this.facingDirection = Constants.LEFT_DIRECTION;
        changeState(State.IDLE);
    }

    public static CrystalGuardian newEnemy(Vector2 pos) {
        return new CrystalGuardian(pos);
    }

    @Override
    public void respawn() {
        this.position = new Vector2(respawnPosition);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        this.facingDirection = Constants.LEFT_DIRECTION;
        this.isOnGround = false;
        this.isDead = false;
        this.hp = Constants.GUARDIAN_HP;
        changeState(State.IDLE);
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
            if (knockbackTimer <= 0)
                velocity.x = 0;
            return;
        }

        if (isDead) {
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta;
            moveY(velocity.y * delta, solidBlocks);
            return;
        }

        velocity.y += Constants.GRAVITY * delta;

        switch (currentState) {
            case IDLE:
                velocity.x = 0;
                if (canSeePlayer(player)) {
                    changeState(State.SHOOT);
                    facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION
                            : Constants.LEFT_DIRECTION;
                }
                break;

            case SHOOT:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= SHOOT_ANIMATION.totalDuration) {
                    changeState(State.FIRE);
                }
                break;

            case FIRE:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= LASER_DURATION) {
                    changeState(State.RUN);
                }
                break;

            case RUN:
                stateTimer += delta;
                runTimer += delta;

                if (runTimer >= RUN_DURATION) {
                    changeState(State.IDLE);
                    runTimer = 0;
                    break;
                }

                boolean playerIsToTheRight = player.position.x > this.position.x;
                boolean playerIsToTheLeft = player.position.x < this.position.x;

                if ((facingDirection == Constants.RIGHT_DIRECTION && playerIsToTheLeft) ||
                        (facingDirection == Constants.LEFT_DIRECTION && playerIsToTheRight)) {
                    changeState(State.TURN);
                    break;
                }

                velocity.x = Constants.GUARDIAN_SPEED * facingDirection;
                boolean hitWall = moveX(velocity.x * delta, solidBlocks);

                if (hitWall) {
                    changeState(State.IDLE);
                    runTimer = 0;
                }
                break;

            case TURN:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= TURN_ANIMATION.totalDuration) {
                    facingDirection *= -1;
                    changeState(State.RUN);
                }
                break;

            case DEAD:
                velocity.x = 0;
                break;
        }

        moveY(velocity.y * delta, solidBlocks);
    }

    // --- State & Animation Management ---
    private void changeState(State newState) {
        State pastState = currentState;
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
                // Animation holds on final frame while firing
                break;
            case RUN:
                if (pastState != State.TURN)
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

    // --- AI & Vision ---
    private boolean canSeePlayer(Player player) {
        if (player.isDead())
            return false;

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

    // --- Laser Mechanics ---
    private float getLaserStartX() {
        return (facingDirection == Constants.RIGHT_DIRECTION)
                ? position.x + Constants.GUARDIAN_HITBOX_WIDTH - LASER_X_OFFSET
                : position.x + LASER_X_OFFSET;
    }

    private float getLaserStartY() {
        return position.y + (Constants.GUARDIAN_HITBOX_HEIGHT / 2f) - (LASER_HEIGHT / 2f) + 5;
    }

    public float getLaserCircleStartX() {
        return (facingDirection == Constants.RIGHT_DIRECTION) ? getLaserStartX() - 12 : getLaserStartX() - 24;
    }

    public float getLaserCircleStartY() {
        return getLaserStartY() - 8;
    }

    public int getLaserAnimationIndex() {
        float period = 0.25f;
        float x = stateTimer % period;

        if (x < period / 4f)
            return 0;
        if (x < period / 2f)
            return 1;
        if (x < 3f * period / 4f)
            return 2;
        return 3;
    }

    public Rectangle getActiveLaserBounds(List<Rectangle> solidBlocks) {
        if (currentState != State.FIRE || isDead)
            return null;

        float startX = getLaserStartX();
        float startY = getLaserStartY();
        float endX = startX + (facingDirection * LASER_MAX_RANGE);

        for (Rectangle solid : solidBlocks) {
            if (solid.y < startY + LASER_HEIGHT && solid.y + solid.height > startY) {
                if (facingDirection == Constants.RIGHT_DIRECTION) {
                    if (solid.x > startX && solid.x < endX)
                        endX = solid.x;
                } else {
                    if (solid.x + solid.width < startX && solid.x + solid.width > endX)
                        endX = solid.x + solid.width;
                }
            }
        }

        float finalX = (facingDirection == Constants.RIGHT_DIRECTION) ? startX : endX;
        float width = Math.abs(endX - startX);

        return new Rectangle(finalX, startY, width, LASER_HEIGHT);
    }

    // --- Properties ---
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
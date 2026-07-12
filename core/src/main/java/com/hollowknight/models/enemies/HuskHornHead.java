package com.hollowknight.models.enemies;

import java.util.List;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class HuskHornHead extends Enemy {

    // --- Constants & Enums ---
    public final static EnemyAnimations WALK_ANIMATION = EnemyAnimations.HORNHEAD_WALK;
    public final static EnemyAnimations IDLE_ANIMATION = EnemyAnimations.HORNHEAD_IDLE;
    public final static EnemyAnimations ATTACK_ANIMATION = EnemyAnimations.HORNHEAD_ATTACK_RUN;
    public final static EnemyAnimations DEATH_ANIMATION = EnemyAnimations.HORNHEAD_DEATH;
    public final static EnemyAnimations ATTACK_START_ANIMATION = EnemyAnimations.HORNHEAD_ATTACK_START;
    public final static EnemyAnimations TURN_ANIMATION = EnemyAnimations.HORNHEAD_TURN;

    public enum State {
        WALK, REST, TURN, ATTACK_START, ATTACK_CHARGE, DEAD
    }

    private static final float SIGHT_RANGE = 700f;
    private static final float SIGHT_HEIGHT_TOLERANCE = Constants.HORNHEAD_HITBOX_HEIGHT - 20;

    // --- State Properties ---
    public State currentState = State.WALK;
    private float stateTimer = 0f;
    private float walkTimer = 0f;

    // --- Initialization & Lifecycle ---
    private HuskHornHead(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        this.hp = Constants.HORNHEAD_HP;
        changeState(State.WALK);
    }

    public static HuskHornHead newEnemy(Vector2 pos) {
        return new HuskHornHead(pos);
    }

    @Override
    public void respawn() {
        this.position = new Vector2(respawnPosition);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        this.facingDirection = Constants.RIGHT_DIRECTION;
        this.isOnGround = false;
        this.isDead = false;
        this.hp = Constants.HORNHEAD_HP;
        changeState(State.WALK);
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

        velocity.y += Constants.GRAVITY * delta;

        switch (currentState) {
            case WALK:
                stateTimer += delta;
                walkTimer += delta;

                if (canSeePlayer(player, solidBlocks)) {
                    changeState(State.ATTACK_START);
                    break;
                }

                if (walkTimer >= Constants.HORNHEAD_WALK_TIMER) {
                    changeState(State.REST);
                    velocity.x = 0;
                    break;
                }

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
                    facingDirection *= -1;
                    changeState(State.WALK);
                }
                break;

            case ATTACK_START:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= ATTACK_START_ANIMATION.totalDuration) {
                    changeState(State.ATTACK_CHARGE);
                }
                break;

            case ATTACK_CHARGE:
                velocity.x = Constants.HORNHEAD_ATTACK_SPEED * facingDirection;
                boolean hitWallCharge = moveX(velocity.x * delta, solidBlocks);

                if (hitWallCharge) {
                    changeState(State.TURN);
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

    // --- AI & Navigation ---
    private boolean isEdgeAhead(List<Rectangle> solids) {
        if (!isOnGround)
            return false;

        float checkX = position.x + (facingDirection == 1 ? Constants.HORNHEAD_HITBOX_WIDTH + 5 : -5);
        float checkY = position.y - 5;

        for (Rectangle solid : solids) {
            if (solid.contains(checkX, checkY)) {
                return false;
            }
        }
        return true;
    }

    private boolean canSeePlayer(Player player, List<Rectangle> solidBlocks) {
        if (player.isDead())
            return false;

        boolean sameHeight = Math.abs(player.position.y - this.position.y) < SIGHT_HEIGHT_TOLERANCE;
        if (!sameHeight)
            return false;

        float distance = player.position.x - this.position.x;
        boolean inRangeAndFacing = false;

        if (facingDirection == Constants.RIGHT_DIRECTION && distance > 0 && distance < SIGHT_RANGE) {
            inRangeAndFacing = true;
        } else if (facingDirection == Constants.LEFT_DIRECTION && distance < 0 && distance > -SIGHT_RANGE) {
            inRangeAndFacing = true;
        }

        if (!inRangeAndFacing)
            return false;

        float startX = Math.min(this.position.x, player.position.x);
        float endX = Math.max(this.position.x, player.position.x);
        float sightY = this.position.y + (Constants.HORNHEAD_HITBOX_HEIGHT / 2f);

        for (Rectangle solid : solidBlocks) {
            if (solid.x + solid.width > startX && solid.x < endX) {
                if (sightY >= solid.y && sightY <= solid.y + solid.height) {
                    return false;
                }
            }
        }
        return true;
    }

    // --- Combat & Properties ---
    @Override
    public void takeDamage(int damage, float sourceX, boolean knockback, float knockbackMultiplier) {
        super.takeDamage(damage, sourceX, knockback, knockbackMultiplier);
        if (knockback) {
            this.velocity.x -= 100;
            this.velocity.y -= 20;
        }
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
}
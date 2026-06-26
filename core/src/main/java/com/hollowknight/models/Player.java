package com.hollowknight.models;

import com.badlogic.gdx.math.Vector2;

public class Player {
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public boolean isOnGround = false;
    private float dashTimer = 0.0f;
    private float dashCooldownTimer = 0.0f;
    private boolean canDash = true;
    private int facingDirection = Constants.RIGHT_DIRECTION;
    private boolean movingHorizontally = false;
    private int jumpsRemaining = 2;

    public PlayerState state = PlayerState.IDLE;
    public float stateTime = 0;

    private int health = 5; // 1-5
    private boolean isInvincible = false;
    private float invincibilityTimer = 0.0f;

    public Player() {

    }

    public Player(Vector2 position) {
        this.position = position;
    }

    public void update(float delta) {
        if (position.y <= 0.001)
            isOnGround = true;
        else
            isOnGround = false;

        if (dashCooldownTimer > 0)
            dashCooldownTimer -= delta;

        if (invincibilityTimer > 0)
            invincibilityTimer -= delta;

        if (isInvincible && invincibilityTimer <= 0) {
            isInvincible = false;
            invincibilityTimer = 0.0f;
        }

        if (state == PlayerState.DASH) {
            dashTimer -= delta;

            velocity.x = Constants.DASH_SPEED * facingDirection;
            velocity.y = 0; // ignore gravity

            position.add(velocity.cpy().scl(delta));

            if (dashTimer <= 0) {
                setStateAfterDash();
                dashCooldownTimer = Constants.DASH_COOLDOWN;
            }

            return;
        }

        if (isOnGround && state != PlayerState.JUMP && state != PlayerState.DOUBLE_JUMP) {
            canDash = true;
            jumpsRemaining = 2;
            if (state == PlayerState.FALL) {
                setSateAfterLanding();
            }
        }

        if (movingHorizontally) {
            velocity.x = facingDirection * Constants.PLAYER_MOVE_SPEED;
        } else {
            velocity.x = 0;
            if (state == PlayerState.RUN) {
                setState(PlayerState.IDLE);
            }
        }

        // Gravity
        if (!isOnGround) {
            velocity.y += Constants.GRAVITY * delta;
            if (velocity.y < 0) {
                setState(PlayerState.FALL);
            }
        } else if (velocity.y < 0.01) {
            velocity.y = 0;
            position.y = 0;
        }

        position.add(velocity.cpy().scl(delta));

        stateTime += delta;
    }

    public void jump() {
        if (canJump()) {
            velocity.y = Constants.JUMP_SPEED;
            jumpsRemaining--;

            if (jumpsRemaining == 0) {
                setState(PlayerState.DOUBLE_JUMP);
            } else {
                setState(PlayerState.JUMP);
            }
        }
    }

    private boolean canJump() {
        return jumpsRemaining > 0 && state != PlayerState.DASH;
    }

    private void move() {
        if (isOnGround)
            setState(PlayerState.RUN);
        movingHorizontally = true;
    }

    public void moveRight() {
        move();
        facingDirection = Constants.RIGHT_DIRECTION;
    }

    public void moveLeft() {
        move();
        facingDirection = Constants.LEFT_DIRECTION;
    }

    public void stopMoving(int dir) {
        if (dir != facingDirection)
            return;
        movingHorizontally = false;
        if (isOnGround)
            setState(PlayerState.IDLE);
    }

    public void dash() {
        if (state != PlayerState.DASH && canDash && dashCooldownTimer <= 0) {
            setState(PlayerState.DASH);
            canDash = false;
            dashTimer = Constants.DASH_DURATION;

            velocity.x = Constants.DASH_SPEED * facingDirection;
            velocity.y = 0;

            setState(PlayerState.DASH);
        }
    }

    private void setStateAfterDash() {
        if (isOnGround && !movingHorizontally) {
            setState(PlayerState.IDLE);
        } else if (movingHorizontally) {
            setState(PlayerState.RUN);
        } else {
            setState(PlayerState.FALL);
        }
    }

    private void setSateAfterLanding() {
        if (movingHorizontally) {
            setState(PlayerState.RUN);
        } else {
            setState(PlayerState.IDLE);
        }
    }

    public int getDirection() {
        return this.facingDirection;
    }

    private void kill() {

    }

    public boolean isInvinvible() {
        return isInvincible;
    }

    public boolean takeDamage() {
        health--;
        isInvincible = true;
        invincibilityTimer = Constants.INVINCIBILITY_TIME;
        if (health <= 0) {
            kill();
            return true;
        }
        return false;
    }

    private void setState(PlayerState newState) {
        if (state != newState) {
            state = newState;
            stateTime = 0;
        }
    }
}

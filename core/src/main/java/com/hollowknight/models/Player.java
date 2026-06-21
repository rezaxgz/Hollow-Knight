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
    public PlayerState state = PlayerState.IDLE;
    public float stateTime = 0;

    public void update(float delta) {
        if (position.y <= 0.001)
            isOnGround = true;
        else
            isOnGround = false;

        if (dashCooldownTimer > 0)
            dashCooldownTimer -= delta;

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

        if (isOnGround) {
            canDash = true;
        }

        if (movingHorizontally) {
            velocity.x = facingDirection * Constants.PLAYER_MOVE_SPEED;
        } else {
            velocity.x = 0;
            if (state == PlayerState.RUN) {
                state = PlayerState.IDLE;
            }
        }

        if (!isOnGround) {
            velocity.y += Constants.GRAVITY * delta;
        } else if (velocity.y < 0.01) {
            velocity.y = 0;
            position.y = 0;
        }

        position.add(velocity.cpy().scl(delta));

        stateTime += delta;
    }

    public void jump() {
        if (isOnGround) {
            velocity.y = 500;
            state = PlayerState.JUMP;
        }
    }

    private void move() {
        if (isOnGround)
            state = PlayerState.RUN;
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
            state = PlayerState.IDLE;
    }

    public void dash() {
        if (state != PlayerState.DASH && canDash && dashCooldownTimer <= 0) {
            state = PlayerState.DASH;
            canDash = false;
            dashTimer = Constants.DASH_DURATION;

            velocity.x = Constants.DASH_SPEED * facingDirection;
            velocity.y = 0;

            state = PlayerState.DASH;
        }
    }

    private void setStateAfterDash() {
        if (isOnGround && !movingHorizontally) {
            state = PlayerState.IDLE;
        } else if (movingHorizontally) {
            state = PlayerState.JUMP;
        } else {
            state = PlayerState.RUN;
        }
    }

    public int getDirection() {
        return this.facingDirection;
    }
}

package com.hollowknight.models.player;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.GameCheat;

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

    private PlayerVitals vitals = new PlayerVitals();
    private boolean isInvincible = false;
    private float invincibilityTimer = 0.0f;

    public Player() {

    }

    public Player(Vector2 position) {
        this.position = position;
    }

    public void update(float delta, List<Rectangle> solidBlocks) {
        vitals.update(delta);
        // --- 1. HANDLE TIMERS ---
        stateTime += delta;

        if (dashCooldownTimer > 0)
            dashCooldownTimer -= delta;
        if (invincibilityTimer > 0)
            invincibilityTimer -= delta;

        if (isInvincible && invincibilityTimer <= 0) {
            isInvincible = false;
            invincibilityTimer = 0.0f;
        }

        // --- 2. HANDLE DASH STATE (Overrides normal physics) ---
        if (state == PlayerState.DASH) {
            dashTimer -= delta;
            velocity.x = Constants.DASH_SPEED * facingDirection;
            velocity.y = 0; // ignore gravity

            isOnGround = false;
            updatePosition(delta, solidBlocks);

            if (dashTimer <= 0) {
                setStateAfterDash();
                dashCooldownTimer = Constants.DASH_COOLDOWN;
            }
            stateTime += delta;
            return; // Exit early so we don't apply normal physics
        }

        // --- 2.5 HANDLE FOCUS STATE (Overrides normal physics & movement) ---
        if (state == PlayerState.FOCUS) {
            velocity.x = 0;
            movingHorizontally = false;

            // Keep player on the ground
            velocity.y += Constants.GRAVITY * delta;
            updatePosition(delta, solidBlocks);

            if (stateTime >= Constants.HEALTH_REFIL_TIME) {
                if (vitals.getSouls() >= Constants.HEALING_COST_IN_SOULS
                        && vitals.getHealth() < Constants.MAX_PLAYER_HEALTH) {
                    vitals.addSouls(-Constants.HEALING_COST_IN_SOULS);
                    vitals.heal(1);

                    if (canFocus()) {
                        focus();
                    } else {
                        stopFocus();
                    }

                } else {
                    setState(PlayerState.IDLE);
                }
            }
            return;
        }

        // --- 3. CALCULATE VELOCITY (Inputs & Gravity) ---
        if (movingHorizontally) {
            velocity.x = facingDirection * Constants.PLAYER_MOVE_SPEED;
        } else {
            velocity.x = 0;
        }

        // ALWAYS apply gravity before moving. This pushes the player into
        // the floor just enough to trigger the collision check next.
        velocity.y += Constants.GRAVITY * delta;

        // --- 4. RESOLVE MOVEMENT & COLLISIONS ---
        isOnGround = false;
        updatePosition(delta, solidBlocks);

        // --- 5. UPDATE PLAYER STATES ---
        if (isOnGround) {
            canDash = true;
            jumpsRemaining = 2;
            velocity.y = 0;

            // Because of our fix below, the state will ALWAYS be FALL
            // when hitting the ground from the air.
            if (state == PlayerState.FALL) {
                setSateAfterLanding();
            }

            // Catch-all to clean up horizontal states if the player stopped moving
            // while the landing animation/logic was processing
            if (!movingHorizontally && state != PlayerState.IDLE) {
                setState(PlayerState.IDLE);
            } else if (movingHorizontally && state != PlayerState.RUN) {
                setState(PlayerState.RUN);
            }
        } else {
            // FIX: If we are moving downwards, we are falling. Period.
            // This allows the jump animations to correctly transition into the fall state.
            if (velocity.y < 0 && state != PlayerState.FALL) {
                setState(PlayerState.FALL);
            }
        }
    }

    private void updatePosition(float delta, List<Rectangle> solids) {
        moveX(velocity.x * delta, solids);

        moveY(velocity.y * delta, solids);
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

    private void attack() {

    }

    private boolean canFocus() {
        return isOnGround && state != PlayerState.DASH && state != PlayerState.FOCUS &&
                vitals.getSouls() > Constants.HEALING_COST_IN_SOULS && vitals.getHealth() < Constants.MAX_PLAYER_HEALTH;
    }

    private void focus() {
        if (canFocus()) {
            setState(PlayerState.FOCUS);
            velocity.x = 0;
            movingHorizontally = false;
            vitals.setNewAnimation(vitals.getSouls(), vitals.getSouls() - Constants.HEALING_COST_IN_SOULS,
                    Constants.HEALTH_REFIL_TIME);
        }
    }

    public void stopFocus() {
        setState(PlayerState.IDLE);
        vitals.resetSouls();
    }

    public void doAction(GameActionType action) {
        if (action != GameActionType.FOCUS && state == PlayerState.FOCUS)
            return;
        switch (action) {
            case MOVE_LEFT -> moveLeft();
            case MOVE_RIGHT -> moveRight();
            case JUMP -> jump();
            case ATTACK -> attack();
            case DASH -> dash();
            case FOCUS -> focus();
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

    public void kill() {

    }

    public boolean isInvinvible() {
        return isInvincible;
    }

    public boolean takeDamage() {
        if (isInvincible)
            return false;
        vitals.takeDamage();
        isInvincible = true;
        invincibilityTimer = Constants.INVINCIBILITY_TIME;
        if (state == PlayerState.FOCUS) { // break focus
            stopFocus();
        }
        if (vitals.isDead()) {
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

    public Rectangle getBounds() {
        return new Rectangle(
                position.x,
                position.y,
                Constants.PLAYER_HITBOX_WIDTH,
                Constants.PLAYER_HITBOX_HEIGHT);
    }

    private void moveX(float amount, List<Rectangle> solids) {
        position.x += amount;

        Rectangle player = getBounds();

        for (Rectangle solid : solids) {
            if (player.overlaps(solid)) {

                if (amount > 0) {
                    position.x = solid.x - player.width;
                } else {
                    position.x = solid.x + solid.width;
                }

                velocity.x = 0;
                break;
            }
        }
    }

    private void moveY(float amount, List<Rectangle> solids) {
        position.y += amount;

        Rectangle bounds = getBounds();

        for (Rectangle solid : solids) {

            if (!bounds.overlaps(solid))
                continue;

            if (amount < 0) { // landing

                position.y = solid.y + solid.height;

                velocity.y = 0;
                isOnGround = true;

            } else { // ceiling

                position.y = solid.y - bounds.height;

                velocity.y = 0;
            }

            return;
        }
    }

    public PlayerVitals getVitals() {
        return vitals;
    }

    public void applyCheat(GameCheat cheat) {
        switch (cheat) {
            case ADD_SOULS -> vitals.addSouls(10);
            case HEAL -> vitals.heal(1);
            case LOSE_SOULS -> vitals.addSouls(-10);
            case TAKE_DAMAGE -> takeDamage();
        }
    }
}

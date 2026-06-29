package com.hollowknight.models.player;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.states.PlayerStatus;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.GameCheat;

public class Player {
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    private float dashTimer = 0.0f;
    private float dashCooldownTimer = 0.0f;

    public PlayerAnimation animation = PlayerAnimation.IDLE;
    public float animationTime = 0;

    public PlayerStatus status = new PlayerStatus();

    private PlayerVitals vitals = new PlayerVitals();

    public Player() {

    }

    public Player(Vector2 position) {
        this.position = position;
    }

    public void update(float delta, List<Rectangle> solidBlocks) {
        vitals.update(delta);
        // --- 1. HANDLE TIMERS ---
        animationTime += delta;

        if (dashCooldownTimer > 0)
            dashCooldownTimer -= delta;

        status.update(delta);

        // --- 2. HANDLE DASH STATE (Overrides normal physics) ---
        if (animation == PlayerAnimation.DASH) {
            dashTimer -= delta;
            velocity.x = Constants.DASH_SPEED * status.getFacingDirection();
            velocity.y = 0; // ignore gravity

            updatePosition(delta, solidBlocks);

            if (dashTimer <= 0) {
                setStateAfterDash();
                dashCooldownTimer = Constants.DASH_COOLDOWN;
            }
            animationTime += delta;
            return; // Exit early so we don't apply normal physics
        }

        // --- 2.5 HANDLE FOCUS STATE (Overrides normal physics & movement) ---
        if (animation == PlayerAnimation.FOCUS) {
            velocity.x = 0;
            status.setMovingHorizontally(false);

            // Keep player on the ground
            velocity.y += Constants.GRAVITY * delta;
            updatePosition(delta, solidBlocks);

            if (animationTime >= Constants.HEALTH_REFIL_TIME) {
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
                    setAnimation(PlayerAnimation.IDLE);
                }
            }
            return;
        }

        // --- 3. CALCULATE VELOCITY (Inputs & Gravity) ---
        if (status.isMovingHorizontally()) {
            velocity.x = status.getFacingDirection() * Constants.PLAYER_MOVE_SPEED;
        } else {
            velocity.x = 0;
        }

        // ALWAYS apply gravity before moving. This pushes the player into
        // the floor just enough to trigger the collision check next.
        velocity.y += Constants.GRAVITY * delta;

        // --- 4. RESOLVE MOVEMENT & COLLISIONS ---
        updatePosition(delta, solidBlocks);
        if (animation == PlayerAnimation.FALL && status.getJumpsRemaining() == 2) {
            status.setRemainingJumps(1);
        }

        // --- 5. UPDATE PLAYER STATES ---
        if (status.isOnGround()) {

            status.resetDash();
            status.resetJumps();

            velocity.y = 0;

            // Because of our fix below, the state will ALWAYS be FALL
            // when hitting the ground from the air.
            if (animation == PlayerAnimation.FALL) {
                setSateAfterLanding();
            }

            // Catch-all to clean up horizontal states if the player stopped moving
            // while the landing animation/logic was processing
            if (!status.isMovingHorizontally() && animation != PlayerAnimation.IDLE) {
                setAnimation(PlayerAnimation.IDLE);
            } else if (status.isMovingHorizontally() && animation != PlayerAnimation.RUN) {
                setAnimation(PlayerAnimation.RUN);
            }
        } else {
            // FIX: If we are moving downwards, we are falling. Period.
            // This allows the jump animations to correctly transition into the fall state.
            if (velocity.y < 0 && animation != PlayerAnimation.FALL) {
                setAnimation(PlayerAnimation.FALL);
            }
        }
    }

    private void updatePosition(float delta, List<Rectangle> solids) {
        status.setOnGround(false);
        moveX(velocity.x * delta, solids);

        moveY(velocity.y * delta, solids);
    }

    public void jump() {
        if (canJump()) {
            velocity.y = Constants.JUMP_SPEED;
            status.useJump();

            if (!status.canJump()) {
                setAnimation(PlayerAnimation.DOUBLE_JUMP);
            } else {
                setAnimation(PlayerAnimation.JUMP);
            }
        }
    }

    private void attack() {

    }

    private boolean canFocus() {
        return status.isOnGround() && animation != PlayerAnimation.DASH && animation != PlayerAnimation.FOCUS &&
                vitals.getSouls() > Constants.HEALING_COST_IN_SOULS && vitals.getHealth() < Constants.MAX_PLAYER_HEALTH;
    }

    private void focus() {
        if (canFocus()) {
            setAnimation(PlayerAnimation.FOCUS);
            velocity.x = 0;
            status.setMovingHorizontally(false);
            vitals.setNewAnimation(vitals.getSouls(), vitals.getSouls() - Constants.HEALING_COST_IN_SOULS,
                    Constants.HEALTH_REFIL_TIME);
        }
    }

    public void stopFocus() {
        setAnimation(PlayerAnimation.IDLE);
        vitals.resetSouls();
    }

    public void doAction(GameActionType action) {
        if (action != GameActionType.FOCUS && animation == PlayerAnimation.FOCUS)
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
        return status.canJump() && animation != PlayerAnimation.DASH;
    }

    private void move() {
        if (status.isOnGround())
            setAnimation(PlayerAnimation.RUN);
        status.setMovingHorizontally(true);
    }

    public void moveRight() {
        move();
        status.setFacingDirection(Constants.RIGHT_DIRECTION);
    }

    public void moveLeft() {
        move();
        status.setFacingDirection(Constants.LEFT_DIRECTION);
    }

    public void stopMoving(int dir) {
        if (dir != status.getFacingDirection())
            return;
        if (animation == PlayerAnimation.FOCUS)
            return;
        status.setMovingHorizontally(false);
        if (status.isOnGround())
            setAnimation(PlayerAnimation.IDLE);
    }

    public void dash() {
        if (animation != PlayerAnimation.DASH && status.canDash() && dashCooldownTimer <= 0) {
            setAnimation(PlayerAnimation.DASH);
            status.consumeDash();
            dashTimer = Constants.DASH_DURATION;

            velocity.x = Constants.DASH_SPEED * status.getFacingDirection();
            velocity.y = 0;

            setAnimation(PlayerAnimation.DASH);
        }
    }

    private void setStateAfterDash() {
        if (status.isOnGround() && !status.isMovingHorizontally()) {
            setAnimation(PlayerAnimation.IDLE);
        } else if (status.isMovingHorizontally()) {
            setAnimation(PlayerAnimation.RUN);
        } else {
            setAnimation(PlayerAnimation.FALL);
        }
    }

    private void setSateAfterLanding() {
        if (status.isMovingHorizontally()) {
            setAnimation(PlayerAnimation.RUN);
        } else {
            setAnimation(PlayerAnimation.IDLE);
        }
    }

    public int getDirection() {
        return status.getFacingDirection();
    }

    public void kill() {

    }

    public boolean isInvinvible() {
        return status.isInvincible();
    }

    public boolean takeDamage() {
        if (status.isInvincible())
            return false;
        vitals.takeDamage();
        status.makeInvincible(Constants.INVINCIBILITY_TIME);
        if (animation == PlayerAnimation.FOCUS) { // break focus
            stopFocus();
        }
        if (vitals.isDead()) {
            kill();
            return true;
        }
        return false;
    }

    private void setAnimation(PlayerAnimation newState) {
        if (animation != newState) {
            animation = newState;
            animationTime = 0;
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
                status.setOnGround(true);

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

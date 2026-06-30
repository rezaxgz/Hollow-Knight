package com.hollowknight.models.player;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.states.CombatState;
import com.hollowknight.models.player.states.MovementState;
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
    public MovementState movementState = MovementState.IDLE;
    public CombatState combatState = CombatState.NONE;

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

        // --- UPDATE THE SPECTATOR BLOCK INSIDE Player.update() ---
        if (status.isSpectatorMode()) {
            // Increased movement speed (e.g., 3x multiplier)
            float spectatorSpeed = Constants.PLAYER_MOVE_SPEED * 3.0f;

            if (status.isMovingHorizontally()) {
                velocity.x = status.getFacingDirection() * spectatorSpeed;
            } else {
                velocity.x = 0;
            }

            // FIX: Read continuous vertical state instead of clearing velocity.y directly
            if (status.isMovingVertically()) {
                velocity.y = status.getVerticalDirection() * spectatorSpeed;
            } else {
                velocity.y = 0;
            }

            // Apply position directly (Bypass collisions and gravity)
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;

            // Disable animations (Freeze at IDLE)
            setAnimation(PlayerAnimation.IDLE);
            animationTime = 0;

            return; // Exit early to skip normal physics and states
        }

        // --- 2. HANDLE DASH STATE (Overrides normal physics) ---
        if (movementState == MovementState.DASH) {
            dashTimer -= delta;
            velocity.x = Constants.DASH_SPEED * status.getFacingDirection();
            velocity.y = 0; // ignore gravity

            updatePosition(delta, solidBlocks);

            if (dashTimer <= 0) {
                setStateAfterDash();
                dashCooldownTimer = Constants.DASH_COOLDOWN;
            }
            updateAnimation();
            return; // Exit early so we don't apply normal physics
        }

        // --- 2.5 HANDLE FOCUS STATE (Overrides normal physics & movement) ---
        if (combatState == CombatState.FOCUS) {
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
                    stopFocus();
                }
            }
            updateAnimation();
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
        if (movementState == MovementState.FALL && status.getJumpsRemaining() == 2) {
            status.setRemainingJumps(1);
        }

        // --- 5. UPDATE PLAYER STATES ---
        if (status.isOnGround()) {

            status.resetDash();
            status.resetJumps();

            velocity.y = 0;

            // State resolution when hitting the ground from the air.
            if (movementState == MovementState.FALL) {
                setStateAfterLanding();
            }

            // Catch-all to clean up horizontal states if the player stopped moving
            // while the landing animation/logic was processing
            if (!status.isMovingHorizontally() && movementState != MovementState.IDLE) {
                movementState = MovementState.IDLE;
            } else if (status.isMovingHorizontally() && movementState != MovementState.RUN) {
                movementState = MovementState.RUN;
            }
        } else {
            // FIX: If we are moving downwards, we are falling. Period.
            // This allows the jump animations to correctly transition into the fall state.
            if (velocity.y < 0 && movementState != MovementState.FALL) {
                movementState = MovementState.FALL;
            }
        }

        // Apply visual updates based on final logic states
        updateAnimation();
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
                movementState = MovementState.DOUBLE_JUMP;
            } else {
                movementState = MovementState.JUMP;
            }
        }
    }

    public void moveVertically(int dir) {
        if (!status.isSpectatorMode())
            return;
        status.moveVertically(dir);
    }

    public void stopVerticalMovement(int releasedDir) {
        if (!status.isSpectatorMode())
            return;

        // Safety check: Only stop if the released key matches our current moving
        // direction
        if (releasedDir == -status.getVerticalDirection()) {
            status.stopVerticalMovement();
            velocity.y = 0;
        }
    }

    private void attack() {
        // Combat state ATTACK logic goes here
    }

    private boolean canFocus() {
        return status.isOnGround() && movementState != MovementState.DASH && combatState != CombatState.FOCUS &&
                vitals.getSouls() > Constants.HEALING_COST_IN_SOULS && vitals.getHealth() < Constants.MAX_PLAYER_HEALTH;
    }

    private void focus() {
        if (canFocus()) {
            combatState = CombatState.FOCUS;
            velocity.x = 0;
            status.setMovingHorizontally(false);
            vitals.setNewAnimation(vitals.getSouls(), vitals.getSouls() - Constants.HEALING_COST_IN_SOULS,
                    Constants.HEALTH_REFIL_TIME);
        }
    }

    public void stopFocus() {
        combatState = CombatState.NONE;

        // Restore appropriate movement state upon exiting focus
        if (status.isOnGround()) {
            movementState = status.isMovingHorizontally() ? MovementState.RUN : MovementState.IDLE;
        } else {
            movementState = MovementState.FALL;
        }

        vitals.resetSouls();
    }

    public void doAction(GameActionType action) {
        if (action != GameActionType.FOCUS && combatState == CombatState.FOCUS)
            return;

        switch (action) {
            case MOVE_LEFT -> moveLeft();
            case MOVE_RIGHT -> moveRight();
            case JUMP -> jump();
            case ATTACK -> attack();
            case DASH -> dash();
            case FOCUS -> focus();
            case DOWN -> moveVertically(Constants.DOWN_DIRECTION);
            case UP -> moveVertically(Constants.UP_DIRECTION);
        }
    }

    private boolean canJump() {
        return status.canJump() && movementState != MovementState.DASH;
    }

    private void move() {
        if (status.isOnGround())
            movementState = MovementState.RUN;
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
        if (combatState == CombatState.FOCUS)
            return;

        status.setMovingHorizontally(false);
        if (status.isOnGround())
            movementState = MovementState.IDLE;
    }

    public void dash() {
        if (movementState != MovementState.DASH && status.canDash() && dashCooldownTimer <= 0) {
            movementState = MovementState.DASH;
            status.consumeDash();
            dashTimer = Constants.DASH_DURATION;

            velocity.x = Constants.DASH_SPEED * status.getFacingDirection();
            velocity.y = 0;
        }
    }

    private void setStateAfterDash() {
        if (status.isOnGround() && !status.isMovingHorizontally()) {
            movementState = MovementState.IDLE;
        } else if (status.isMovingHorizontally()) {
            movementState = MovementState.RUN;
        } else {
            movementState = MovementState.FALL;
        }
    }

    private void setStateAfterLanding() {
        if (status.isMovingHorizontally()) {
            movementState = MovementState.RUN;
        } else {
            movementState = MovementState.IDLE;
        }
    }

    public int getDirection() {
        return status.getFacingDirection();
    }

    public void kill() {
        combatState = CombatState.DEAD;
    }

    public boolean isInvinvible() {
        return status.isInvincible();
    }

    public boolean takeDamage() {
        if (status.isInvincible())
            return false;

        vitals.takeDamage();
        status.makeInvincible(Constants.INVINCIBILITY_TIME);

        if (combatState == CombatState.FOCUS) {
            stopFocus();
        }

        if (vitals.isDead()) {
            kill();
            return true;
        }

        // Optionally trigger a hurt state here:
        // combatState = CombatState.HURT;

        return false;
    }

    // --- NEW: Centralized Animation Chooser ---
    private void updateAnimation() {
        PlayerAnimation targetAnimation = animation;

        if (status.isSpectatorMode()) {
            targetAnimation = PlayerAnimation.IDLE;
        }
        // Combat States take visual priority
        else if (combatState == CombatState.DEAD) {
            targetAnimation = PlayerAnimation.DEAD;
        } else if (combatState == CombatState.FOCUS) {
            targetAnimation = PlayerAnimation.FOCUS;
        } else if (combatState == CombatState.HURT) {
            targetAnimation = PlayerAnimation.IDLE_HURT;
        } else {
            // If no active overriding combat state, derive from movement
            switch (movementState) {
                case DASH -> targetAnimation = PlayerAnimation.DASH;
                case DOUBLE_JUMP -> targetAnimation = PlayerAnimation.DOUBLE_JUMP;
                case JUMP -> targetAnimation = PlayerAnimation.JUMP;
                case FALL -> targetAnimation = PlayerAnimation.FALL;
                case RUN -> targetAnimation = PlayerAnimation.RUN;
                case IDLE -> targetAnimation = PlayerAnimation.IDLE;
            }
        }

        setAnimation(targetAnimation);
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
            case HEAL -> vitals.heal(1);
            case TAKE_DAMAGE -> takeDamage();
            case FILL_SOULS -> vitals.addSouls(Constants.MAX_PLAYER_SOULS);
            case GOD_MODE -> status.toggleGodMode();
            case SPECTATOR_MODE -> {
                status.toggleSpectatorMode();
                if (!status.isSpectatorMode()) {
                    status.stopVerticalMovement();
                    velocity.y = 0;
                }
            }
            case KILL_ENEMIES -> {
            }
        }
    }
}
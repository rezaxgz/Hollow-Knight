package com.hollowknight.models.player.states;

import com.hollowknight.models.Constants;

public class PlayerStatus {

    // Movement
    private boolean onGround = true;
    private boolean movingHorizontally = false;
    private int facingDirection = Constants.RIGHT_DIRECTION;
    private boolean movementLocked = false;

    // Directional Holding state
    private boolean holdingUp = false;
    private boolean holdingDown = false;
    private boolean holdingLeft = false;
    private boolean holdingRight = false;

    // Abilities
    private boolean canDash = true;
    private int jumpsRemaining = 2;

    // Damage
    private boolean godMode = false;
    private boolean invincible = false;
    private float invincibilityTimer = 0;

    // Ability Timers
    private float dashTimer = 0.0f;
    private float dashCooldownTimer = 0.0f;
    private float attackTimer = 0.0f;
    private float attackCooldownTimer = 0.0f;
    private float knockbackTimer = 0.0f;
    private float screamTimer = 0.0f;
    private int screamTicksApplied = 0;
    private float castTimer = 0.0f;

    // Spectator
    private boolean spectatorMode = false;
    private boolean movingVertically = false;
    private int verticalDir = 0;

    // Wall Slide and Wall Jump
    private boolean touchingWall = false;
    private int wallDirection = 0;
    private float wallJumpTimer = 0.0f;

    // -------------------------------------------------
    // Core Update
    // -------------------------------------------------

    public void update(float delta) {
        if (invincible) {
            invincibilityTimer -= delta;

            if (invincibilityTimer <= 0f) {
                invincible = false;
                invincibilityTimer = 0f;
            }
        }

        // Update generic ability/combat timers
        if (wallJumpTimer > 0)
            wallJumpTimer -= delta;
        if (dashCooldownTimer > 0)
            dashCooldownTimer -= delta;
        if (dashTimer > 0)
            dashTimer -= delta;
        if (attackTimer > 0)
            attackTimer -= delta;
        if (attackCooldownTimer > 0)
            attackCooldownTimer -= delta;
        if (knockbackTimer > 0)
            knockbackTimer -= delta;
        if (screamTimer > 0)
            screamTimer -= delta;
        if (castTimer > 0)
            castTimer -= delta;
    }

    // -------------------------------------------------
    // Ground
    // -------------------------------------------------

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;

        if (onGround) {
            canDash = true;
            jumpsRemaining = 2;
        }
    }

    // -------------------------------------------------
    // Movement & Locks
    // -------------------------------------------------

    public boolean isMovingHorizontally() {
        return movingHorizontally;
    }

    public void setMovingHorizontally(boolean movingHorizontally) {
        this.movingHorizontally = movingHorizontally;
    }

    public int getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(int facingDirection) {
        this.facingDirection = facingDirection;
    }

    public boolean isMovementLocked() {
        return movementLocked;
    }

    public void setMovementLocked(boolean movementLocked) {
        this.movementLocked = movementLocked;
    }

    // -------------------------------------------------
    // Directional Holding Setters & Getters
    // -------------------------------------------------

    public boolean isHoldingUp() {
        return holdingUp;
    }

    public void setHoldingUp(boolean holdingUp) {
        this.holdingUp = holdingUp;
    }

    public boolean isHoldingDown() {
        return holdingDown;
    }

    public void setHoldingDown(boolean holdingDown) {
        this.holdingDown = holdingDown;
    }

    public boolean isHoldingLeft() {
        return holdingLeft;
    }

    public void setHoldingLeft(boolean holdingLeft) {
        this.holdingLeft = holdingLeft;
    }

    public boolean isHoldingRight() {
        return holdingRight;
    }

    public void setHoldingRight(boolean holdingRight) {
        this.holdingRight = holdingRight;
    }

    // -------------------------------------------------
    // Timers Getters & Setters
    // -------------------------------------------------

    public float getDashTimer() {
        return dashTimer;
    }

    public void setDashTimer(float dashTimer) {
        this.dashTimer = dashTimer;
    }

    public float getDashCooldownTimer() {
        return dashCooldownTimer;
    }

    public void setDashCooldownTimer(float dashCooldownTimer) {
        this.dashCooldownTimer = dashCooldownTimer;
    }

    public float getAttackTimer() {
        return attackTimer;
    }

    public void setAttackTimer(float attackTimer) {
        this.attackTimer = attackTimer;
    }

    public float getAttackCooldownTimer() {
        return attackCooldownTimer;
    }

    public void setAttackCooldownTimer(float attackCooldownTimer) {
        this.attackCooldownTimer = attackCooldownTimer;
    }

    public float getKnockbackTimer() {
        return knockbackTimer;
    }

    public void setKnockbackTimer(float knockbackTimer) {
        this.knockbackTimer = knockbackTimer;
    }

    public float getScreamTimer() {
        return screamTimer;
    }

    public void setScreamTimer(float screamTimer) {
        this.screamTimer = screamTimer;
    }

    public int getScreamTicksApplied() {
        return screamTicksApplied;
    }

    public void setScreamTicksApplied(int screamTicksApplied) {
        this.screamTicksApplied = screamTicksApplied;
    }

    public float getCastTimer() {
        return castTimer;
    }

    public void setCastTimer(float castTimer) {
        this.castTimer = castTimer;
    }

    // -------------------------------------------------
    // Jump
    // -------------------------------------------------

    public int getJumpsRemaining() {
        return jumpsRemaining;
    }

    public boolean canJump() {
        return jumpsRemaining > 0;
    }

    public void useJump() {
        if (jumpsRemaining > 0)
            jumpsRemaining--;
    }

    public void resetJumps() {
        jumpsRemaining = 2;
    }

    public void setRemainingJumps(int n) {
        jumpsRemaining = n;
    }

    // -------------------------------------------------
    // Dash
    // -------------------------------------------------

    public boolean canDash() {
        return canDash;
    }

    public void consumeDash() {
        canDash = false;
    }

    public void resetDash() {
        canDash = true;
    }

    // -------------------------------------------------
    // Invincibility
    // -------------------------------------------------

    public void toggleGodMode() {
        godMode = !godMode;
    }

    public boolean isInvincible() {
        return invincible || godMode;
    }

    public void makeInvincible(float duration) {
        invincible = true;
        invincibilityTimer = duration;
    }

    public boolean shouldFlash() {
        // Only flash if the player is actually in their post-damage invincibility
        // window
        if (invincible && invincibilityTimer > 0) {
            // 0.2f is the total cycle time. < 0.1f means it will be invisible 50% of the
            // time.
            return (invincibilityTimer % 0.2f) < 0.1f;
        }
        return false;
    }

    // -------------------------------------------------
    // Utility
    // -------------------------------------------------

    public void resetAirAbilities() {
        resetDash();
        resetJumps();
    }

    // -------------------------------------------------
    // Spectator
    // -------------------------------------------------

    public void toggleSpectatorMode() {
        spectatorMode = !spectatorMode;
        godMode = spectatorMode;
    }

    public boolean isSpectatorMode() {
        return spectatorMode;
    }

    public boolean isMovingVertically() {
        return movingVertically;
    }

    public int getVerticalDirection() {
        return verticalDir;
    }

    public void moveVertically(int dir) {
        verticalDir = dir;
        movingVertically = true;
    }

    public void stopVerticalMovement() {
        verticalDir = 0;
        movingVertically = false;
    }

    // -------------------------------------------------
    // wall utils
    // -------------------------------------------------

    public boolean isTouchingWall() {
        return touchingWall;
    }

    public void setTouchingWall(boolean touchingWall) {
        this.touchingWall = touchingWall;
    }

    public int getWallDirection() {
        return wallDirection;
    }

    public void setWallDirection(int wallDirection) {
        this.wallDirection = wallDirection;
    }

    public float getWallJumpTimer() {
        return wallJumpTimer;
    }

    public void setWallJumpTimer(float wallJumpTimer) {
        this.wallJumpTimer = wallJumpTimer;
    }
}
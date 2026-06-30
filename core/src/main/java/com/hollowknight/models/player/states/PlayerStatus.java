package com.hollowknight.models.player.states;

import com.hollowknight.models.Constants;

public class PlayerStatus {

    // Movement
    private boolean onGround = true;
    private boolean movingHorizontally = false;
    private int facingDirection = Constants.RIGHT_DIRECTION;

    // Abilities
    private boolean canDash = true;
    private int jumpsRemaining = 2;

    // Damage
    private boolean godMode = false;
    private boolean invincible = false;
    private float invincibilityTimer = 0;

    // Spectator
    private boolean spectatorMode = false;
    private boolean movingVertically = false;
    private int verticalDir = 0;

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
    // Movement
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

    public void rechargeAirJump() {
        jumpsRemaining = Math.max(jumpsRemaining, 1);
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

    public void update(float delta) {
        if (invincible) {
            invincibilityTimer -= delta;

            if (invincibilityTimer <= 0f) {
                invincible = false;
                invincibilityTimer = 0f;
            }
        }
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
}
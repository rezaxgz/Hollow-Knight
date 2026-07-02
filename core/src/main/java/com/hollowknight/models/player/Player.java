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
    public Vector2 respawnPosition;
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();

    private float dashTimer = 0.0f;
    private float dashCooldownTimer = 0.0f;

    // --- NEW: Attack Tracking ---
    private float attackTimer = 0.0f;
    private float attackCooldown = 0f;
    private PlayerAnimation currentAttackAnimation = PlayerAnimation.IDLE;

    public PlayerAnimation animation = PlayerAnimation.IDLE;
    public float animationTime = 0;

    public PlayerStatus status = new PlayerStatus();
    public MovementState movementState = MovementState.IDLE;
    public CombatState combatState = CombatState.NONE;

    private PlayerVitals vitals = new PlayerVitals();

    private float knockbackTimer = 0.0f;

    public Player(Vector2 position) {
        this.position = new Vector2(position);
        this.respawnPosition = new Vector2(position);
    }

    public void update(float delta, List<Rectangle> solidBlocks) {
        vitals.update(delta);
        // --- 1. HANDLE TIMERS ---
        animationTime += delta;

        if (dashCooldownTimer > 0)
            dashCooldownTimer -= delta;

        // Handle Attack Timer ---
        if (attackTimer > 0) {
            attackTimer -= delta;
            if (attackTimer <= 0 && combatState == CombatState.ATTACK) {
                combatState = CombatState.NONE;
            }
        }

        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }

        if (combatState == CombatState.DEAD && animation == PlayerAnimation.DEAD
                && animationTime >= Constants.PLAYER_DEATH_TIME) {
            // death timer over; respawn
            respawn();
            return;
        }

        status.update(delta);

        // --- UPDATE THE SPECTATOR BLOCK INSIDE Player.update() ---
        if (status.isSpectatorMode()) {
            float spectatorSpeed = Constants.PLAYER_MOVE_SPEED * 3.0f;

            if (status.isMovingHorizontally()) {
                velocity.x = status.getFacingDirection() * spectatorSpeed;
            } else {
                velocity.x = 0;
            }

            if (status.isMovingVertically()) {
                velocity.y = status.getVerticalDirection() * spectatorSpeed;
            } else {
                velocity.y = 0;
            }

            position.x += velocity.x * delta;
            position.y += velocity.y * delta;

            setAnimation(PlayerAnimation.IDLE);
            animationTime = 0;

            return;
        }

        // --- 1.5 HANDLE HURT STATE (Overrides normal physics/input) ---
        if (combatState == CombatState.HURT) {
            knockbackTimer -= delta;

            // Apply gravity so the player arcs backwards instead of floating linearly
            velocity.y += Constants.GRAVITY * delta;
            updatePosition(delta, solidBlocks);

            if (knockbackTimer <= 0) {
                combatState = CombatState.NONE;
                velocity.x = 0; // Stop horizontal sliding when control is returned
            }

            updateAnimation();
            return; // Skip the rest of the movement logic!
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
            return;
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

            if (movementState == MovementState.FALL) {
                setStateAfterLanding();
            }

            if (!status.isMovingHorizontally() && movementState != MovementState.IDLE) {
                movementState = MovementState.IDLE;
            } else if (status.isMovingHorizontally() && movementState != MovementState.RUN) {
                movementState = MovementState.RUN;
            }
        } else {
            if (velocity.y < 0 && movementState != MovementState.FALL) {
                movementState = MovementState.FALL;
            }
        }

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

    public boolean shouldFlash() {
        return status.shouldFlash();
    }

    public void moveVertically(int dir) {
        if (!status.isSpectatorMode())
            return;
        status.moveVertically(dir);
    }

    public void stopVerticalMovement(int releasedDir) {
        if (!status.isSpectatorMode())
            return;

        if (releasedDir == -status.getVerticalDirection()) {
            status.stopVerticalMovement();
            velocity.y = 0;
        }
    }

    // --- NEW: Attack Logic ---
    private void attack() {
        // Prevent attacking if dead, focusing, or already attacking
        if (combatState == CombatState.DEAD || combatState == CombatState.FOCUS || combatState == CombatState.ATTACK
                || attackCooldown > 0) {
            return;
        }

        combatState = CombatState.ATTACK;
        attackTimer = Constants.SLASH_TIME;

        // Determine attack direction and animation
        if (velocity.y > 0) {
            currentAttackAnimation = PlayerAnimation.UP_SLASH;
        } else if (!status.isOnGround() && velocity.y < 0) {
            // Down slashes typically only happen while in the air
            currentAttackAnimation = PlayerAnimation.DOWN_SLASH;
        } else {
            // Standard horizontal slash (Randomize between normal and alt)
            currentAttackAnimation = Math.random() > 0.5 ? PlayerAnimation.SLASH : PlayerAnimation.SLASH_ALT;
        }
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

    public boolean takeDamage(int amount, float sourceX) {
        if (status.isInvincible())
            return false;

        vitals.takeDamage(amount);
        status.makeInvincible(Constants.INVINCIBILITY_TIME);

        if (combatState == CombatState.FOCUS) {
            stopFocus();
        }

        if (vitals.isDead()) {
            kill();
            return true;
        }

        // --- NEW: TRIGGER KNOCKBACK ---
        combatState = CombatState.HURT;
        knockbackTimer = Constants.KNOCKBACK_DURATION;

        // Determine knockback direction (away from source)
        float knockbackDir = (position.x < sourceX) ? -1f : 1f;

        velocity.x = Constants.KNOCKBACK_SPEED_X * knockbackDir;
        velocity.y = Constants.KNOCKBACK_SPEED_Y; // Slight pop into the air

        // Ensure they are treated as airborne
        status.setOnGround(false);
        movementState = MovementState.FALL;

        return false;
    }

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
        } else if (combatState == CombatState.ATTACK) {
            targetAnimation = currentAttackAnimation;
        } else if (combatState == CombatState.HURT) {
            targetAnimation = PlayerAnimation.IDLE_HURT;
        } else {
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

    private void respawn() {
        position = new Vector2(respawnPosition);
        velocity = new Vector2();
        vitals.heal(Constants.MAX_PLAYER_HEALTH);
        combatState = CombatState.NONE;
        status.setFacingDirection(Constants.RIGHT_DIRECTION);
        status.setMovingHorizontally(false);
    }

    public PlayerVitals getVitals() {
        return vitals;
    }

    public void applyCheat(GameCheat cheat) {
        switch (cheat) {
            case HEAL -> vitals.heal(1);
            case TAKE_DAMAGE -> takeDamage(1, position.x + 10);
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

    public Rectangle getAttackHitbox() {
        if (combatState != CombatState.ATTACK)
            return null;

        float width = Constants.PLAYER_HITBOX_WIDTH;
        float height = Constants.PLAYER_HITBOX_HEIGHT;

        // Define the reach of the slash
        float attackRangeX = width * 1.5f;
        float attackRangeY = height * 1.2f;

        if (currentAttackAnimation == PlayerAnimation.UP_SLASH) {
            return new Rectangle(position.x, position.y + height, width, attackRangeY);
        } else if (currentAttackAnimation == PlayerAnimation.DOWN_SLASH) {
            return new Rectangle(position.x, position.y - attackRangeY, width, attackRangeY);
        } else {
            // Horizontal slash
            if (status.getFacingDirection() == Constants.RIGHT_DIRECTION) {
                return new Rectangle(position.x + width, position.y, attackRangeX, height);
            } else {
                return new Rectangle(position.x - attackRangeX, position.y, attackRangeX, height);
            }
        }
    }
}
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

    // =========================================================================================
    // FIELDS & PROPERTIES
    // =========================================================================================

    public Vector2 respawnPosition;
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();

    // Timers
    private float dashTimer = 0.0f;
    private float dashCooldownTimer = 0.0f;
    private float attackTimer = 0.0f;
    private float attackCooldown = 0f;
    private float knockbackTimer = 0.0f;
    public float animationTime = 0;

    // States & Vitals
    public PlayerAnimation animation = PlayerAnimation.IDLE;
    private PlayerAnimation currentAttackAnimation = PlayerAnimation.IDLE;
    public PlayerStatus status = new PlayerStatus();
    public MovementState movementState = MovementState.IDLE;
    public CombatState combatState = CombatState.NONE;
    private PlayerVitals vitals = new PlayerVitals();

    // =========================================================================================
    // CONSTRUCTOR
    // =========================================================================================

    public Player(Vector2 position) {
        this.position = new Vector2(position);
        this.respawnPosition = new Vector2(position);
    }

    // =========================================================================================
    // CORE GAME LOOP (UPDATE)
    // =========================================================================================

    public void update(float delta, List<Rectangle> solidBlocks) {
        vitals.update(delta);
        updateTimers(delta);

        if (checkDeathAndRespawn())
            return;

        status.update(delta);

        // State handlers - if any of these return true, they override normal
        // movement/physics
        if (handleSpectatorMode(delta))
            return;
        if (handleHurtState(delta, solidBlocks))
            return;
        if (handleDashState(delta, solidBlocks))
            return;
        if (handleFocusState(delta, solidBlocks))
            return;

        // Normal movement & gravity
        applyNormalPhysics(delta);
        updatePosition(delta, solidBlocks);
        resolveMovementStates();

        updateAnimation();
    }

    // =========================================================================================
    // UPDATE HANDLERS (Extracted from update())
    // =========================================================================================

    private void updateTimers(float delta) {
        animationTime += delta;

        if (dashCooldownTimer > 0)
            dashCooldownTimer -= delta;

        if (attackTimer > 0) {
            attackTimer -= delta;
            if (attackTimer <= 0 && combatState == CombatState.ATTACK) {
                combatState = CombatState.NONE;
            }
        }

        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }
    }

    private boolean checkDeathAndRespawn() {
        if (combatState == CombatState.DEAD && animation == PlayerAnimation.DEAD
                && animationTime >= Constants.PLAYER_DEATH_TIME) {
            respawn();
            return true;
        }
        return false;
    }

    private boolean handleSpectatorMode(float delta) {
        if (!status.isSpectatorMode())
            return false;

        float spectatorSpeed = Constants.PLAYER_MOVE_SPEED * 3.0f;

        velocity.x = status.isMovingHorizontally() ? status.getFacingDirection() * spectatorSpeed : 0;
        velocity.y = status.isMovingVertically() ? status.getVerticalDirection() * spectatorSpeed : 0;

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        setAnimation(PlayerAnimation.IDLE);
        animationTime = 0;

        return true;
    }

    private boolean handleHurtState(float delta, List<Rectangle> solidBlocks) {
        if (combatState != CombatState.HURT)
            return false;

        knockbackTimer -= delta;

        // Apply gravity for a clean backwards arc
        velocity.y += Constants.GRAVITY * delta;
        updatePosition(delta, solidBlocks);

        if (knockbackTimer <= 0) {
            combatState = CombatState.NONE;
            velocity.x = 0; // Stop horizontal slide
        }

        updateAnimation();
        return true;
    }

    private boolean handleDashState(float delta, List<Rectangle> solidBlocks) {
        if (movementState != MovementState.DASH)
            return false;

        dashTimer -= delta;
        velocity.x = Constants.DASH_SPEED * status.getFacingDirection();
        velocity.y = 0; // Ignore gravity while dashing

        updatePosition(delta, solidBlocks);

        if (dashTimer <= 0) {
            setStateAfterDash();
            dashCooldownTimer = Constants.DASH_COOLDOWN;
        }

        updateAnimation();
        return true;
    }

    private boolean handleFocusState(float delta, List<Rectangle> solidBlocks) {
        if (combatState != CombatState.FOCUS)
            return false;

        velocity.x = 0;
        status.setMovingHorizontally(false);

        // Keep player grounded
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
        return true;
    }

    private void applyNormalPhysics(float delta) {
        velocity.x = status.isMovingHorizontally() ? status.getFacingDirection() * Constants.PLAYER_MOVE_SPEED : 0;
        velocity.y += Constants.GRAVITY * delta;
    }

    private void resolveMovementStates() {
        // Prevent floating double jump if walking off a ledge
        if (movementState == MovementState.FALL && status.getJumpsRemaining() == 2) {
            status.setRemainingJumps(1);
        }

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
    }

    // =========================================================================================
    // ACTION CONTROLLERS (Input handling)
    // =========================================================================================

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
        if (dir != status.getFacingDirection() || combatState == CombatState.FOCUS)
            return;

        status.setMovingHorizontally(false);
        if (status.isOnGround())
            movementState = MovementState.IDLE;
    }

    public void jump() {
        if (status.canJump() && movementState != MovementState.DASH) {
            velocity.y = Constants.JUMP_SPEED;
            status.useJump();
            movementState = !status.canJump() ? MovementState.DOUBLE_JUMP : MovementState.JUMP;
        }
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

    public void moveVertically(int dir) {
        if (status.isSpectatorMode())
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

    // =========================================================================================
    // COMBAT & DAMAGE
    // =========================================================================================

    private void attack() {
        if (combatState == CombatState.DEAD || combatState == CombatState.FOCUS || combatState == CombatState.ATTACK
                || attackCooldown > 0) {
            return;
        }

        combatState = CombatState.ATTACK;
        attackTimer = Constants.SLASH_TIME;

        if (velocity.y > 0) {
            currentAttackAnimation = PlayerAnimation.UP_SLASH;
        } else if (!status.isOnGround() && velocity.y < 0) {
            currentAttackAnimation = PlayerAnimation.DOWN_SLASH;
        } else {
            currentAttackAnimation = Math.random() > 0.5 ? PlayerAnimation.SLASH : PlayerAnimation.SLASH_ALT;
        }
    }

    public boolean takeDamage(int amount, float sourceX) {
        if (status.isInvincible())
            return false;

        vitals.takeDamage(amount);
        status.makeInvincible(Constants.INVINCIBILITY_TIME);

        if (combatState == CombatState.FOCUS)
            stopFocus();

        if (vitals.isDead()) {
            kill();
            return true;
        }

        // Trigger Knockback
        combatState = CombatState.HURT;
        knockbackTimer = Constants.KNOCKBACK_DURATION;
        float knockbackDir = (position.x < sourceX) ? -1f : 1f;

        velocity.x = Constants.KNOCKBACK_SPEED_X * knockbackDir;
        velocity.y = Constants.KNOCKBACK_SPEED_Y;

        status.setOnGround(false);
        movementState = MovementState.FALL;

        return false;
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

    public void kill() {
        combatState = CombatState.DEAD;
    }

    private void respawn() {
        position.set(respawnPosition);
        velocity.setZero();
        vitals.heal(Constants.MAX_PLAYER_HEALTH);
        combatState = CombatState.NONE;
        status.setFacingDirection(Constants.RIGHT_DIRECTION);
        status.setMovingHorizontally(false);
    }

    // =========================================================================================
    // COLLISION & PHYSICS HELPERS
    // =========================================================================================

    private void updatePosition(float delta, List<Rectangle> solids) {
        status.setOnGround(false);
        moveX(velocity.x * delta, solids);
        moveY(velocity.y * delta, solids);
    }

    private void moveX(float amount, List<Rectangle> solids) {
        position.x += amount;
        Rectangle player = getBounds();

        for (Rectangle solid : solids) {
            if (player.overlaps(solid)) {
                position.x = (amount > 0) ? solid.x - player.width : solid.x + solid.width;
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

            if (amount < 0) { // Landing
                position.y = solid.y + solid.height;
                velocity.y = 0;
                status.setOnGround(true);
            } else { // Hitting ceiling
                position.y = solid.y - bounds.height;
                velocity.y = 0;
            }
            return;
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
        movementState = status.isMovingHorizontally() ? MovementState.RUN : MovementState.IDLE;
    }

    // =========================================================================================
    // ANIMATION & UTILITY
    // =========================================================================================

    private void updateAnimation() {
        PlayerAnimation targetAnimation = animation;

        if (status.isSpectatorMode()) {
            targetAnimation = PlayerAnimation.IDLE;
        } else if (combatState == CombatState.DEAD) {
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
        return new Rectangle(position.x, position.y, Constants.PLAYER_HITBOX_WIDTH, Constants.PLAYER_HITBOX_HEIGHT);
    }

    public Rectangle getAttackHitbox() {
        if (combatState != CombatState.ATTACK)
            return null;

        float width = Constants.PLAYER_HITBOX_WIDTH;
        float height = Constants.PLAYER_HITBOX_HEIGHT;
        float attackRangeX = width * 1.5f;
        float attackRangeY = height * 1.2f;

        if (currentAttackAnimation == PlayerAnimation.UP_SLASH) {
            return new Rectangle(position.x, position.y + height, width, attackRangeY);
        } else if (currentAttackAnimation == PlayerAnimation.DOWN_SLASH) {
            return new Rectangle(position.x, position.y - attackRangeY, width, attackRangeY);
        } else {
            return status.getFacingDirection() == Constants.RIGHT_DIRECTION
                    ? new Rectangle(position.x + width, position.y, attackRangeX, height)
                    : new Rectangle(position.x - attackRangeX, position.y, attackRangeX, height);
        }
    }

    public boolean shouldFlash() {
        return status.shouldFlash();
    }

    public boolean isInvinvible() {
        return status.isInvincible();
    }

    public int getDirection() {
        return status.getFacingDirection();
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
}
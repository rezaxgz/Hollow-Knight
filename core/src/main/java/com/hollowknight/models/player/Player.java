package com.hollowknight.models.player;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.states.CombatState;
import com.hollowknight.models.player.states.MovementState;
import com.hollowknight.models.player.states.PlayerStatus;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.views.GameAssetManager;

public class Player {

    // =========================================================================================
    // FIELDS & PROPERTIES
    // =========================================================================================

    public Vector2 respawnPosition;
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();

    // Timers & Triggers left public for external systems (e.g. GameWorld)
    public float animationTime = 0;
    public boolean triggerSpiritCast = false;
    public boolean triggerScreamDamage = false;

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
        status.update(delta); // Updates all ability/combat timers and invincibility
        updateTimers(delta);

        if (checkDeathAndRespawn())
            return;

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
        if (handleScreamState(delta, solidBlocks))
            return;
        if (handleCastState(delta, solidBlocks))
            return;

        // Normal movement & gravity
        applyNormalPhysics(delta);
        updatePosition(delta, solidBlocks);
        resolveMovementStates();

        updateAnimation();
    }

    // =========================================================================================
    // UPDATE HANDLERS
    // =========================================================================================

    private void updateTimers(float delta) {
        animationTime += delta;

        if (status.getAttackTimer() <= 0 && combatState == CombatState.ATTACK) {
            combatState = CombatState.NONE;
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

        // Apply gravity for a clean backwards arc
        velocity.y += Constants.GRAVITY * delta;
        updatePosition(delta, solidBlocks);

        if (status.getKnockbackTimer() <= 0) {
            combatState = CombatState.NONE;
            velocity.x = 0; // Stop horizontal slide
        }

        updateAnimation();
        return true;
    }

    private boolean handleDashState(float delta, List<Rectangle> solidBlocks) {
        if (movementState != MovementState.DASH)
            return false;

        velocity.x = Constants.DASH_SPEED * status.getFacingDirection();
        velocity.y = 0; // Ignore gravity while dashing

        updatePosition(delta, solidBlocks);

        if (status.getDashTimer() <= 0) {
            setStateAfterDash();
            status.setDashCooldownTimer(Constants.DASH_COOLDOWN);
        }

        updateAnimation();
        return true;
    }

    private boolean handleFocusState(float delta, List<Rectangle> solidBlocks) {
        if (combatState != CombatState.FOCUS)
            return false;

        // Keep player grounded while locked
        velocity.y += Constants.GRAVITY * delta;
        updatePosition(delta, solidBlocks);

        if (animationTime >= Constants.HEALTH_REFIL_TIME) {
            if (vitals.getSouls() >= Constants.HEALING_COST_IN_SOULS
                    && vitals.getHealth() < Constants.MAX_PLAYER_HEALTH) {

                vitals.addSouls(-Constants.HEALING_COST_IN_SOULS);
                vitals.heal(1);
                AudioController.getInstance().playSfx(GameAssetManager.focusHealSfx);
                AudioController.getInstance().playSfx(GameAssetManager.focusReadySfx);

                if (canFocus()) {
                    animationTime = 0; // Reset focus timer loop
                    vitals.setNewAnimation(vitals.getSouls(), vitals.getSouls() - Constants.HEALING_COST_IN_SOULS,
                            Constants.HEALTH_REFIL_TIME);
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

    private boolean handleScreamState(float delta, List<Rectangle> solidBlocks) {
        if (combatState != CombatState.SCREAM)
            return false;

        // Player is suspended in air while screaming (X lock is handled by
        // lockMovement)
        velocity.y = 0;

        // "Damage is dealt in 3 ticks"
        float tickInterval = Constants.SOUL_SCREAM_TIME / 3.0f;
        int expectedTicks = 3 - (int) (status.getScreamTimer() / tickInterval);

        if (expectedTicks > status.getScreamTicksApplied() && expectedTicks <= 3) {
            status.setScreamTicksApplied(status.getScreamTicksApplied() + 1);
            triggerScreamDamage = true; // Flips to true for exactly one frame per tick
        }

        // End Scream
        if (status.getScreamTimer() <= 0) {
            combatState = CombatState.NONE;
            movementState = status.isOnGround() ? MovementState.IDLE : MovementState.FALL;
            unlockMovement();
        }

        updateAnimation();
        return true;
    }

    private boolean handleCastState(float delta, List<Rectangle> solidBlocks) {
        if (combatState != CombatState.CAST)
            return false;

        // Player is suspended in air while casting (X lock is handled by lockMovement)
        velocity.y = 0;

        if (status.getCastTimer() <= 0) {
            combatState = CombatState.NONE;
            movementState = status.isOnGround() ? MovementState.IDLE : MovementState.FALL;
            unlockMovement();
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
        switch (action) {
            case MOVE_LEFT -> moveLeft();
            case MOVE_RIGHT -> moveRight();
            case JUMP -> jump();
            case ATTACK -> attack();
            case DASH -> dash();
            case FOCUS -> focus();
            case DOWN -> moveVertically(Constants.DOWN_DIRECTION);
            case UP -> moveVertically(Constants.UP_DIRECTION);
            case SCREAM -> soulScream();
            case SPRITE_CAST -> spiritCast();
        }
    }

    private void move() {
        if (status.isOnGround())
            movementState = MovementState.RUN;
        status.setMovingHorizontally(true);
    }

    public void moveRight() {
        if (status.isMovementLocked())
            return;
        move();
        status.setFacingDirection(Constants.RIGHT_DIRECTION);
    }

    public void moveLeft() {
        if (status.isMovementLocked())
            return;
        move();
        status.setFacingDirection(Constants.LEFT_DIRECTION);
    }

    public void stopMoving(int dir) {
        if (dir != status.getFacingDirection() || status.isMovementLocked())
            return;

        status.setMovingHorizontally(false);
        if (status.isOnGround())
            movementState = MovementState.IDLE;
    }

    public void jump() {
        if (status.isMovementLocked())
            return;
        if (status.canJump() && movementState != MovementState.DASH) {
            velocity.y = Constants.JUMP_SPEED;
            status.useJump();
            movementState = !status.canJump() ? MovementState.DOUBLE_JUMP : MovementState.JUMP;
            if (movementState == MovementState.JUMP) {
                AudioController.getInstance().playSfx(GameAssetManager.jumpSfx);
            } else {
                AudioController.getInstance().playSfx(GameAssetManager.evadeSfx);
            }
        }
    }

    public void dash() {
        if (status.isMovementLocked())
            return;
        if (movementState != MovementState.DASH && status.canDash() && status.getDashCooldownTimer() <= 0) {
            movementState = MovementState.DASH;
            status.consumeDash();
            status.setDashTimer(Constants.DASH_DURATION);
            AudioController.getInstance().playSfx(GameAssetManager.dashSfx);
            velocity.x = Constants.DASH_SPEED * status.getFacingDirection();
            velocity.y = 0;
        }
    }

    public void moveVertically(int dir) {
        if (dir == Constants.UP_DIRECTION)
            status.setHoldingUp(true);
        if (dir == Constants.DOWN_DIRECTION)
            status.setHoldingDown(true);

        if (status.isSpectatorMode())
            status.moveVertically(-dir);
    }

    public void stopVerticalMovement(int releasedDir) {
        if (releasedDir == Constants.UP_DIRECTION)
            status.setHoldingUp(false);
        if (releasedDir == Constants.DOWN_DIRECTION)
            status.setHoldingDown(false);

        if (!status.isSpectatorMode())
            return;

        if (releasedDir == -status.getVerticalDirection()) {
            status.stopVerticalMovement();
            velocity.y = 0;
        }
    }

    // =========================================================================================
    // MOVEMENT LOCK UTILS
    // =========================================================================================

    public void lockMovement() {
        status.setMovementLocked(true);
        status.setMovingHorizontally(false);
        velocity.x = 0;
    }

    public void unlockMovement() {
        status.setMovementLocked(false);
    }

    // =========================================================================================
    // COMBAT & DAMAGE
    // =========================================================================================

    private void attack() {
        if (combatState == CombatState.DEAD || status.isMovementLocked() || combatState == CombatState.ATTACK
                || status.getAttackCooldownTimer() > 0) {
            return;
        }

        combatState = CombatState.ATTACK;
        status.setAttackTimer(Constants.SLASH_TIME);

        if (status.isHoldingUp()) {
            currentAttackAnimation = PlayerAnimation.UP_SLASH;
        } else if (!status.isOnGround() && status.isHoldingDown()) {
            currentAttackAnimation = PlayerAnimation.DOWN_SLASH;
        } else {
            currentAttackAnimation = Math.random() > 0.5 ? PlayerAnimation.SLASH : PlayerAnimation.SLASH_ALT;
        }
    }

    public void soulScream() {
        if (combatState == CombatState.DEAD || status.isMovementLocked() || combatState == CombatState.SCREAM) {
            return;
        }

        if (vitals.getSouls() >= Constants.ABILITY_COST) {
            vitals.addSouls(-Constants.ABILITY_COST);

            lockMovement();
            combatState = CombatState.SCREAM;
            status.setScreamTimer(Constants.SOUL_SCREAM_TIME);
            status.setScreamTicksApplied(0);
            triggerScreamDamage = false;
        }
    }

    public void spiritCast() {
        if (combatState == CombatState.DEAD || status.isMovementLocked() || combatState == CombatState.CAST) {
            return;
        }

        if (vitals.getSouls() >= Constants.ABILITY_COST) {
            vitals.addSouls(-Constants.ABILITY_COST);

            lockMovement();
            combatState = CombatState.CAST;
            status.setCastTimer(Constants.SPIRIT_CAST_TIME);
            triggerSpiritCast = true; // Signals GameWorld to spawn exactly one projectile
        }
    }

    public void pogo() {
        velocity.y = Constants.JUMP_SPEED;
        status.resetDash();
        // Give the player exactly 1 jump to use in mid-air (avoids resetting to 2)
        status.setRemainingJumps(Math.max(status.getJumpsRemaining(), 1));
    }

    public boolean takeDamage(int amount, float sourceX) {
        if (status.isInvincible())
            return false;

        vitals.takeDamage(amount);
        AudioController.getInstance().playSfx(GameAssetManager.enemyHurtSfx);
        status.makeInvincible(Constants.INVINCIBILITY_TIME);

        // Break out of locks cleanly
        if (combatState == CombatState.FOCUS) {
            stopFocus();
        } else {
            unlockMovement();
        }

        if (vitals.isDead()) {
            kill();
            return true;
        }

        // Trigger Knockback
        combatState = CombatState.HURT;
        status.setKnockbackTimer(Constants.KNOCKBACK_DURATION);
        float knockbackDir = (position.x < sourceX) ? -1f : 1f;

        velocity.x = Constants.KNOCKBACK_SPEED_X * knockbackDir;
        velocity.y = Constants.KNOCKBACK_SPEED_Y;

        status.setOnGround(false);
        movementState = MovementState.FALL;

        return false;
    }

    private boolean canFocus() {
        return status.isOnGround() && movementState != MovementState.DASH &&
                vitals.getSouls() >= Constants.HEALING_COST_IN_SOULS
                && vitals.canHeal();
    }

    private void focus() {
        if (canFocus()) {
            if (combatState != CombatState.FOCUS) {
                combatState = CombatState.FOCUS;
                lockMovement();
            }
            vitals.setNewAnimation(vitals.getSouls(), vitals.getSouls() - Constants.HEALING_COST_IN_SOULS,
                    Constants.HEALTH_REFIL_TIME);
            animationTime = 0; // Essential for subsequent loop checks
            AudioController.getInstance().playSfx(GameAssetManager.focusChargingSfx);
        }
    }

    public void stopFocus() {
        if (combatState == CombatState.FOCUS) {
            GameAssetManager.focusChargingSfx.stop();
            combatState = CombatState.NONE;
            unlockMovement();

            if (status.isOnGround()) {
                movementState = status.isMovingHorizontally() ? MovementState.RUN : MovementState.IDLE;
            } else {
                movementState = MovementState.FALL;
            }

            vitals.resetSouls();
        }
    }

    public void kill() {
        // Don't allow instant death on god mode
        if (status.isInvincible() && !vitals.isDead())
            return;
        AudioController.getInstance().playSfx(GameAssetManager.deathSfx);
        status.setMovementLocked(true);
        status.stopVerticalMovement();
        status.setMovingHorizontally(false);
        velocity.x = 0;
        velocity.y = 0;
        combatState = CombatState.DEAD;
    }

    public boolean isDead() {
        return combatState == CombatState.DEAD;
    }

    private void respawn() {
        position.set(respawnPosition);
        velocity.setZero();
        vitals.heal(Constants.MAX_PLAYER_HEALTH);
        combatState = CombatState.NONE;
        status.setFacingDirection(Constants.RIGHT_DIRECTION);
        status.setMovingHorizontally(false);
        status.setMovementLocked(false);
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
        } else if (combatState == CombatState.SCREAM) {
            targetAnimation = PlayerAnimation.SCREAM;
        } else if (combatState == CombatState.CAST) {
            targetAnimation = PlayerAnimation.CAST;
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

    public Rectangle getScreamHitbox() {
        if (combatState != CombatState.SCREAM)
            return null;

        float width = Constants.SOUL_SCREAM_HITBOX_WIDTH;
        float height = Constants.SOUL_SCREAM_HITBOX_HEIGHT;

        return new Rectangle(
                position.x - (width - Constants.PLAYER_HITBOX_WIDTH) / 2f, // Centered on X
                position.y + Constants.PLAYER_HITBOX_HEIGHT, // Placed directly above head
                width,
                height);
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
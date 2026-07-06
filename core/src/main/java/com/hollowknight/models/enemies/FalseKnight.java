package com.hollowknight.models.enemies;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class FalseKnight extends Enemy {
    public final static EnemyAnimations IDLE_ANIMATION = EnemyAnimations.FALSE_KNIGHT_IDLE;
    public final static EnemyAnimations ATTACK_ANTICIPATE_ANIMATION = EnemyAnimations.FALSE_KNIGHT_ATTACK_ANTICIPATE;
    public final static EnemyAnimations ATTACK_ANIMATION = EnemyAnimations.FALSE_KNIGHT_ATTACK;
    public final static EnemyAnimations ATTACK_RECOVER_ANIMATION = EnemyAnimations.FALSE_KNIGHT_ATTACK_RECOVER;
    public final static EnemyAnimations RUN_ANTICIPATE_ANIMATION = EnemyAnimations.FALSE_KNIGHT_RUN_ANTICIPATE;
    public final static EnemyAnimations RUN_ANIMATION = EnemyAnimations.FALSE_KNIGHT_RUN;
    public final static EnemyAnimations JUMP_ANTICIPATE_ANIMATION = EnemyAnimations.FALSE_KNIGHT_JUMP_ANTICIPATE;
    public final static EnemyAnimations JUMP_ATTACK_ANIMATION = EnemyAnimations.FALSE_KNIGHT_JUMP_ATTACK;
    public final static EnemyAnimations JUMP_ANIMATION = EnemyAnimations.FALSE_KNIGHT_JUMP;
    public final static EnemyAnimations LAND_ANIMATION = EnemyAnimations.FALSE_KNIGHT_LAND;
    public final static EnemyAnimations DEATH_ANIMATION = EnemyAnimations.FALSE_KNIGHT_DEATH;

    // --- ENEMY STATES ---
    public enum State {
        IDLE, ATTACK_ANTICIPATE, ATTACK, ATTACK_RECOVER,
        RUN_ANTICIPATE, RUN,
        JUMP_ANTICIPATE, JUMP_ATTACK, POWER_JUMP_ATTACK, LAND, JUMP_BACK, // Added POWER_JUMP_ATTACK
        DEAD
    }

    private enum ActionType {
        ATTACK, RUN, JUMP, POWER_JUMP // Added POWER_JUMP
    }

    public State currentState = State.IDLE;

    // Timers & Variables
    private float stateTimer = 0f;

    private final Random random = new Random();
    private ActionType lastAction = null;
    private int lastActionRepeatCount = 0;
    private boolean pendingJumpIsDefensive = false;

    // Tracks damage taken over a short rolling window to trigger the
    // "jump back" panic response when the player deals a lot of damage fast.
    private int recentDamageTaken = 0;
    private float recentDamageTimer = 0f;

    public boolean triggerShockwave = false;
    private boolean isPhaseTwo = false;
    private ActionType pendingJumpType = null;

    // New Tunables for the Power Slam
    private static final float POWER_JUMP_SPEED_X = 200f;
    private static final float POWER_JUMP_SPEED_Y = 650f; // Noticeably higher than standard 520f

    // --- Tunables ---
    private static final float IDLE_THINK_TIME = 0.4f; // Small pause before he picks his next move

    // Distance thresholds used to weight the decision
    private static final float CLOSE_RANGE = 140f;
    private static final float FAR_RANGE = 400f;

    private static final float RUN_SPEED = 220f;
    private static final float RUN_MAX_DURATION = 4f; // Safety cap so he can't run forever

    private static final float JUMP_ATTACK_SPEED_X = 260f;
    private static final float JUMP_ATTACK_SPEED_Y = 520f;

    private static final float DEFENSIVE_JUMP_SPEED_X = 300f;
    private static final float DEFENSIVE_JUMP_SPEED_Y = 480f;

    private static final float MACE_HITBOX_WIDTH = 70f;
    private static final float MACE_HITBOX_HEIGHT = 60f;

    private static final int BODY_CONTACT_DAMAGE = 4;
    private static final int JUMP_ATTACK_DAMAGE = 6;

    private static final float DAMAGE_BURST_WINDOW = 2.5f; // Rolling window length
    private static final int DAMAGE_BURST_THRESHOLD = 25; // Damage within the window that triggers a jump back

    private static final int MAX_CONSECUTIVE_REPEATS = 2; // Never pick the same move more than this many times in a row

    private FalseKnight(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        changeState(State.IDLE);
        this.hp = Constants.FALSE_KNIGHT_HP;
        this.facingDirection = Constants.LEFT_DIRECTION;
    }

    public static FalseKnight newEnemy(Vector2 pos) {
        return new FalseKnight(pos);
    }

    // Bosses don't respawn once they're dead - this is intentionally a no-op.
    // GameWorld also special-cases FalseKnight so this is never even called
    // while he's alive, but overriding it as a no-op is a safety net.
    @Override
    public void respawn() {
        if (isDead)
            return;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        animationTime += delta;

        // Decay the damage-burst window
        if (recentDamageTimer > 0) {
            recentDamageTimer -= delta;
            if (recentDamageTimer <= 0) {
                recentDamageTimer = 0;
                recentDamageTaken = 0;
            }
        }

        // 1. Handle Knockback
        if (knockbackTimer > 0) {
            knockbackTimer -= delta;
            velocity.y += Constants.GRAVITY * delta;
            isOnGround = false;
            moveX(velocity.x * delta, solidBlocks);
            moveY(velocity.y * delta, solidBlocks);
            if (knockbackTimer <= 0)
                velocity.x = 0;
            return;
        }

        // 2. Handle Death
        if (isDead) {
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta;
            moveY(velocity.y * delta, solidBlocks);
            return;
        }

        // 3. Reactive interrupt: took too much damage too fast, jump back
        if (canTriggerDefensiveJump()) {
            beginDefensiveJump();
        }

        // Apply gravity for active states
        velocity.y += Constants.GRAVITY * delta;

        // 4. State Machine
        switch (currentState) {
            case IDLE:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= IDLE_THINK_TIME) {
                    decideNextAction(player);
                }
                break;

            case ATTACK_ANTICIPATE:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= ATTACK_ANTICIPATE_ANIMATION.totalDuration) {
                    changeState(State.ATTACK);
                }
                break;

            case ATTACK:
                velocity.x = 0;
                stateTimer += delta;
                // Damage is applied by GameWorld via getActiveAttackHitbox() while this
                // state is active - see checkFalseKnightAttacks in GameWorld.
                if (stateTimer >= ATTACK_ANIMATION.totalDuration) {
                    changeState(State.ATTACK_RECOVER);
                }
                break;

            case ATTACK_RECOVER:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= ATTACK_RECOVER_ANIMATION.totalDuration) {
                    changeState(State.IDLE);
                }
                break;

            case RUN_ANTICIPATE:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= RUN_ANTICIPATE_ANIMATION.totalDuration) {
                    changeState(State.RUN);
                }
                break;

            case RUN:
                stateTimer += delta;

                // Follows the player while running
                facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION
                        : Constants.LEFT_DIRECTION;
                velocity.x = RUN_SPEED * facingDirection;
                boolean hitWall = moveX(velocity.x * delta, solidBlocks);

                float distanceToPlayer = Math.abs(player.position.x - position.x);
                if (hitWall || distanceToPlayer <= CLOSE_RANGE || stateTimer >= RUN_MAX_DURATION) {
                    changeState(State.IDLE);
                }
                break;
            // Update your JUMP_ANTICIPATE case:
            case JUMP_ANTICIPATE:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= JUMP_ANTICIPATE_ANIMATION.totalDuration) {
                    if (pendingJumpIsDefensive) {
                        launchDefensiveJump(player);
                    } else if (pendingJumpType == ActionType.POWER_JUMP) {
                        launchPowerJump(player); // Launch the new high jump
                    } else {
                        launchOffensiveJump(player);
                    }
                }
                break;

            // Add this entirely new case under JUMP_ATTACK:
            case POWER_JUMP_ATTACK:
                // Airborne - wait for gravity to bring him back down
                if (isOnGround) {
                    triggerShockwave = true; // Signal GameWorld to spawn the wave!
                    changeState(State.LAND);
                }
                break;

            case JUMP_ATTACK:
                // Airborne - just wait for gravity to bring him back down
                if (isOnGround) {
                    changeState(State.LAND);
                }
                break;

            case JUMP_BACK:
                if (isOnGround) {
                    changeState(State.IDLE);
                }
                break;

            case LAND:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= LAND_ANIMATION.totalDuration) {
                    changeState(State.IDLE);
                }
                break;

            case DEAD:
                velocity.x = 0;
                break;
        }

        // Vertical collision
        moveY(velocity.y * delta, solidBlocks);
    }

    // --- Decision System ---
    private void decideNextAction(Player player) {
        float distance = Math.abs(player.position.x - position.x);
        ActionType action = chooseNextAction(distance);

        // Lock in the direction he's committing to at the moment of the decision
        facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;

        if (action == lastAction) {
            lastActionRepeatCount++;
        } else {
            lastAction = action;
            lastActionRepeatCount = 1;
        }

        switch (action) {
            case ATTACK:
                changeState(State.ATTACK_ANTICIPATE);
                break;
            case RUN:
                changeState(State.RUN_ANTICIPATE);
                break;
            case JUMP:
                pendingJumpIsDefensive = false;
                pendingJumpType = ActionType.JUMP; // Track standard jump
                changeState(State.JUMP_ANTICIPATE);
                break;
            case POWER_JUMP:
                pendingJumpIsDefensive = false;
                pendingJumpType = ActionType.POWER_JUMP; // Track power jump
                changeState(State.JUMP_ANTICIPATE);
                break;
        }
    }

    private ActionType chooseNextAction(float distance) {
        float attackWeight;
        float runWeight;
        float jumpWeight;
        float powerJumpWeight = 0f; // New weight

        if (distance <= CLOSE_RANGE) {
            attackWeight = 70f;
            runWeight = 10f;
            jumpWeight = 20f;
        } else if (distance >= FAR_RANGE) {
            attackWeight = 5f;
            runWeight = 55f;
            jumpWeight = 40f;
            if (isPhaseTwo)
                powerJumpWeight = 25f; // Add chance to power jump from afar
        } else {
            attackWeight = 35f;
            runWeight = 35f;
            jumpWeight = 30f;
            if (isPhaseTwo)
                powerJumpWeight = 20f; // Add chance to power jump from mid-range
        }

        // Apply anti-repeat penalties (Add POWER_JUMP to your existing switch
        // statement)
        if (lastAction != null) {
            float penalty = (lastActionRepeatCount >= MAX_CONSECUTIVE_REPEATS) ? 0f : 0.35f;
            switch (lastAction) {
                case ATTACK -> attackWeight *= penalty;
                case RUN -> runWeight *= penalty;
                case JUMP -> jumpWeight *= penalty;
                case POWER_JUMP -> powerJumpWeight *= penalty;
            }
        }

        float total = attackWeight + runWeight + jumpWeight + powerJumpWeight;
        if (total <= 0f) {
            attackWeight = runWeight = jumpWeight = 1f;
            total = 3f;
        }

        float roll = random.nextFloat() * total;
        if (roll < attackWeight)
            return ActionType.ATTACK;
        roll -= attackWeight;
        if (roll < runWeight)
            return ActionType.RUN;
        roll -= runWeight;
        if (roll < jumpWeight)
            return ActionType.JUMP;
        return ActionType.POWER_JUMP;
    }

    // --- Defensive Jump (reactive, not part of the weighted choice) ---
    private boolean canTriggerDefensiveJump() {
        if (recentDamageTaken < DAMAGE_BURST_THRESHOLD)
            return false;

        // Don't interrupt a jump that's already happening, or the death state
        return currentState != State.JUMP_ANTICIPATE
                && currentState != State.JUMP_ATTACK
                && currentState != State.JUMP_BACK
                && currentState != State.LAND
                && currentState != State.DEAD;
    }

    private void launchPowerJump(Player player) {
        facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;
        velocity.x = POWER_JUMP_SPEED_X * facingDirection;
        velocity.y = POWER_JUMP_SPEED_Y;
        isOnGround = false;
        changeState(State.POWER_JUMP_ATTACK);
    }

    private void beginDefensiveJump() {
        recentDamageTaken = 0;
        recentDamageTimer = 0;
        pendingJumpIsDefensive = true;
        changeState(State.JUMP_ANTICIPATE);
    }

    private void launchOffensiveJump(Player player) {
        facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;
        velocity.x = JUMP_ATTACK_SPEED_X * facingDirection;
        velocity.y = JUMP_ATTACK_SPEED_Y;
        isOnGround = false;
        changeState(State.JUMP_ATTACK);
    }

    private void launchDefensiveJump(Player player) {
        // Jump away from wherever the player currently is
        int awayDirection = player.position.x > position.x ? Constants.LEFT_DIRECTION : Constants.RIGHT_DIRECTION;
        facingDirection = awayDirection;
        velocity.x = DEFENSIVE_JUMP_SPEED_X * awayDirection;
        velocity.y = DEFENSIVE_JUMP_SPEED_Y;
        isOnGround = false;
        changeState(State.JUMP_BACK);
    }

    // --- State & Animation Manager ---
    private void changeState(State newState) {
        currentState = newState;
        stateTimer = 0f;

        switch (newState) {
            case IDLE:
                setAnimation(IDLE_ANIMATION);
                break;
            case ATTACK_ANTICIPATE:
                setAnimation(ATTACK_ANTICIPATE_ANIMATION);
                break;
            case ATTACK:
                setAnimation(ATTACK_ANIMATION);
                break;
            case ATTACK_RECOVER:
                setAnimation(ATTACK_RECOVER_ANIMATION);
                break;
            case RUN_ANTICIPATE:
                setAnimation(RUN_ANTICIPATE_ANIMATION);
                break;
            case RUN:
                setAnimation(RUN_ANIMATION);
                break;
            case JUMP_ANTICIPATE:
                setAnimation(JUMP_ANTICIPATE_ANIMATION);
                break;
            case JUMP_ATTACK:
                setAnimation(JUMP_ATTACK_ANIMATION);
                break;
            case JUMP_BACK:
                setAnimation(JUMP_ANIMATION);
                break;
            case LAND:
                setAnimation(LAND_ANIMATION);
                break;
            case DEAD:
                setAnimation(DEATH_ANIMATION);
                break;
            case POWER_JUMP_ATTACK:
                setAnimation(JUMP_ATTACK_ANIMATION);
                break;
            default:
                break;
        }
    }

    private void setAnimation(EnemyAnimations newAnimation) {
        if (animation != newAnimation) {
            animation = newAnimation;
            animationTime = 0;
        }
    }

    @Override
    public void kill() {
        isDead = true;
        velocity.set(0, 0);
        changeState(State.DEAD);
    }

    @Override
    public void takeDamage(int damage, float sourceX) {
        super.takeDamage(damage, sourceX);

        if (isDead)
            return;

        // Phase 2 activation check
        if (!isPhaseTwo && hp <= Constants.FALSE_KNIGHT_HP / 2) {
            isPhaseTwo = true;
        }

        // Feed the damage-burst tracker used by the defensive jump trigger
        recentDamageTaken += damage;
        recentDamageTimer = DAMAGE_BURST_WINDOW;
    }

    @Override
    public int getCollisionDamage() {
        return (currentState == State.JUMP_ATTACK) ? JUMP_ATTACK_DAMAGE : BODY_CONTACT_DAMAGE;
    }

    /**
     * The mace's actual damage hitbox, separate from his body. Only active
     * during the ATTACK state. GameWorld checks this the same way it checks
     * CrystalGuardian's laser bounds.
     */
    public Rectangle getActiveAttackHitbox() {
        if (currentState != State.ATTACK || isDead) {
            return null;
        }

        float x = (facingDirection == Constants.RIGHT_DIRECTION)
                ? position.x + Constants.FALSE_KNIGHT_HITBOX_WIDTH
                : position.x - MACE_HITBOX_WIDTH;
        float y = position.y;

        return new Rectangle(x, y, MACE_HITBOX_WIDTH, MACE_HITBOX_HEIGHT);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y,
                Constants.FALSE_KNIGHT_HITBOX_WIDTH, Constants.FALSE_KNIGHT_HITBOX_HEIGHT);
    }
}

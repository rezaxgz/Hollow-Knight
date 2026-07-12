package com.hollowknight.models.enemies;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.Player;

public class FalseKnight extends Enemy {

    // --- NEW CONSTANTS ---
    public static final int FALSE_KNIGHT_HITBOX_WIDTH = 260;
    public static final int FALSE_KNIGHT_HITBOX_HEIGHT = 350;
    public static final int FALSE_KNIGHT_HP = 1000;
    public static final int FALSE_KNIGHT_ATTACK_DAMAGE = 2;

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
        JUMP_ANTICIPATE, NORMAL_JUMP, POWER_JUMP_ATTACK, LAND, JUMP_BACK,
        DEAD, STUNNED, STUN_RECOVER
    }

    private enum ActionType {
        ATTACK, RUN, NORMAL_JUMP, POWER_JUMP, DEFENSIVE_JUMP
    }

    public State currentState = State.IDLE;

    // Timers & Variables
    private float stateTimer = 0f;
    private final Random random = new Random();
    private final Deque<ActionType> recentActions = new ArrayDeque<>();
    private ActionType lastAction = null;
    private int consecutiveActionCount = 0;
    private int recentDamageTaken = 0;
    private float recentDamageTimer = 0f;
    private float defensiveJumpCooldown = 0f;
    private float defensiveJumpReactionTimer = -1f;
    private float idleDecisionDelay = IDLE_THINK_TIME;
    private float runStartX = 0f;
    private int runDirection = Constants.LEFT_DIRECTION;

    private boolean hasArenaLimits = false;
    private float arenaMinX = 0f;
    private float arenaMaxX = 0f;

    public boolean triggerShockwave = false;
    private boolean isPhaseTwo = false;
    private ActionType pendingJumpType = null;

    public boolean triggerHeavyShake = false;
    public boolean triggerNormalShake = false;
    public boolean triggerAttackShake = false;

    // Phase 2 & Stun Variables
    private boolean hasBeenStunned = false;
    private int damageTakenDuringStun = 0;
    private float currentRunSpeed = RUN_SPEED; // Default RUN_SPEED
    private float currentIdleThinkTime = IDLE_THINK_TIME; // Default IDLE_THINK_TIME
    private float timeScale = 1f; // Used to scale animation and state time globally

    // Shockwave Tracker
    public List<Shockwave> shockwaves = new ArrayList<>();

    // Tunables
    private static final float STUN_DURATION = 9.0f;
    private static final float POWER_JUMP_SPEED_Y = 900f;
    private static final float IDLE_THINK_TIME = 0.4f;
    private static final float CLOSE_RANGE = 140f;
    private static final float FAR_RANGE = 400f;
    private static final float RUN_SPEED = 400f;
    private static final float RUN_MAX_DURATION = 4f;
    private static final float RUN_MAX_TRAVEL_DISTANCE = 850f;
    private static final float RUN_WALL_SAFETY_MARGIN = 64f;
    private static final float RUN_CORNER_PLAYER_MARGIN = 120f;
    private static final float RUN_CORNER_STOP_DISTANCE = 320f;

    private static final float JUMP_ATTACK_SPEED_X = 260f;
    public static final float JUMP_ATTACK_SPEED_Y = 800f;
    private static final float JUMP_TARGET_RANDOM_OFFSET = 90f;

    private static final float DEFENSIVE_JUMP_SPEED_X = 300f;
    private static final float DEFENSIVE_JUMP_SPEED_Y = 750f;
    private static final float DEFENSIVE_JUMP_COOLDOWN = 3.5f;
    private static final float DEFENSIVE_REACTION_MIN_DELAY = 0.08f;
    private static final float DEFENSIVE_REACTION_MAX_DELAY = 0.28f;

    private static final int BODY_CONTACT_DAMAGE = 4;
    private static final int JUMP_ATTACK_DAMAGE = 6;
    private static final float DAMAGE_BURST_WINDOW = 2.5f;
    private static final int DAMAGE_BURST_THRESHOLD = 25;
    private static final int ACTION_HISTORY_SIZE = 3;
    private static final int MAX_CONSECUTIVE_ACTIONS = 2;
    private static final float IMMEDIATE_REPEAT_WEIGHT_MULTIPLIER = 0.18f;
    private static final float FALSE_KNIGHT_KNOCKBACK_MULTIPLIER = 0.25f;
    private static final float FALSE_KNIGHT_KNOCKBACK_DURATION_SCALE = 0.5f;

    public FalseKnight(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        changeState(State.IDLE);
        this.hp = FALSE_KNIGHT_HP;
        this.facingDirection = Constants.LEFT_DIRECTION;
        this.type = EnemyType.FALSE_KNIGHT;
    }

    public static FalseKnight newEnemy(Vector2 pos) {
        return new FalseKnight(pos);
    }

    /**
     * Supplies the two horizontal arena walls so charge runs can stop before the
     * False Knight traps the player against either corner.
     */
    public void setArenaWalls(Rectangle firstWall, Rectangle secondWall) {
        if (firstWall == null || secondWall == null)
            return;

        Rectangle leftWall = firstWall.x <= secondWall.x ? firstWall : secondWall;
        Rectangle rightWall = firstWall.x <= secondWall.x ? secondWall : firstWall;
        float minX = leftWall.x + leftWall.width;
        float maxX = rightWall.x;

        if (maxX - minX <= FALSE_KNIGHT_HITBOX_WIDTH)
            return;

        arenaMinX = minX;
        arenaMaxX = maxX;
        hasArenaLimits = true;
    }

    @Override
    public void respawn() {
        if (isDead)
            return;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        // Apply scaled time for animations and state checks
        float scaledDelta = delta * timeScale;
        animationTime += scaledDelta;

        // Update active shockwaves
        Iterator<Shockwave> iter = shockwaves.iterator();
        while (iter.hasNext()) {
            Shockwave wave = iter.next();
            wave.update(delta); // Unscaled delta for physics entities
            if (wave.lifetime > wave.maxLifetime) {
                iter.remove();
            }
        }

        if (recentDamageTimer > 0) {
            recentDamageTimer -= delta; // Real time tracking
            if (recentDamageTimer <= 0) {
                recentDamageTimer = 0;
                recentDamageTaken = 0;
                defensiveJumpReactionTimer = -1f;
            }
        }

        if (defensiveJumpCooldown > 0f)
            defensiveJumpCooldown = Math.max(0f, defensiveJumpCooldown - delta);

        if (defensiveJumpReactionTimer > 0f)
            defensiveJumpReactionTimer = Math.max(0f, defensiveJumpReactionTimer - delta);

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

        if (isDead) {
            velocity.x = 0;
            velocity.y += Constants.GRAVITY * delta;
            moveY(velocity.y * delta, solidBlocks);
            return;
        }

        if (canTriggerDefensiveJump()) {
            launchDefensiveJump(player); // Instantly jump back without anticipation
        }

        velocity.y += Constants.GRAVITY * delta;

        switch (currentState) {
            case IDLE:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= idleDecisionDelay)
                    decideNextAction(player);
                break;
            case ATTACK_ANTICIPATE:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= ATTACK_ANTICIPATE_ANIMATION.totalDuration)
                    changeState(State.ATTACK);
                break;
            case ATTACK:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= ATTACK_ANIMATION.totalDuration) {
                    changeState(State.ATTACK_RECOVER);
                    triggerAttackShake = true;
                }
                break;
            case ATTACK_RECOVER:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= ATTACK_RECOVER_ANIMATION.totalDuration)
                    changeState(State.IDLE);
                break;
            case RUN_ANTICIPATE:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= RUN_ANTICIPATE_ANIMATION.totalDuration)
                    changeState(State.RUN);
                break;
            case RUN:
                stateTimer += scaledDelta;
                facingDirection = runDirection;
                velocity.x = currentRunSpeed * runDirection;

                boolean reachedTravelLimit = Math.abs(position.x - runStartX) >= RUN_MAX_TRAVEL_DISTANCE;
                boolean reachedArenaSafetyLimit = wouldCrossArenaRunLimit(velocity.x * delta);
                boolean playerCorneredAhead = isPlayerCorneredAhead(player, runDirection)
                        && Math.abs(player.position.x - position.x) <= RUN_CORNER_STOP_DISTANCE;
                boolean playerPassedBehind = hasPlayerPassedBehind(player, runDirection);

                if (reachedTravelLimit || reachedArenaSafetyLimit || playerCorneredAhead || playerPassedBehind) {
                    velocity.x = 0f;
                    changeState(State.IDLE);
                    break;
                }

                boolean hitWall = moveX(velocity.x * delta, solidBlocks);
                float distanceToPlayer = Math.abs(player.position.x - position.x);
                if (hitWall || distanceToPlayer <= CLOSE_RANGE || stateTimer >= RUN_MAX_DURATION) {
                    changeState(State.IDLE);
                }
                break;
            case JUMP_ANTICIPATE:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= JUMP_ANTICIPATE_ANIMATION.totalDuration) {
                    if (pendingJumpType == ActionType.POWER_JUMP) {
                        launchPowerJump(player);
                    } else {
                        launchNormalJump(player);
                    }
                }
                break;
            case POWER_JUMP_ATTACK:
                moveX(velocity.x * delta, solidBlocks);
                if (isOnGround) {
                    triggerShockwave = true;
                    triggerHeavyShake = true;
                    // Spawn floor shockwave at edge of hitbox
                    float spawnX = facingDirection == Constants.RIGHT_DIRECTION
                            ? position.x + FALSE_KNIGHT_HITBOX_WIDTH
                            : position.x - 40;
                    shockwaves.add(new Shockwave(spawnX, position.y, facingDirection));
                    changeState(State.LAND);
                }
                break;
            case NORMAL_JUMP:
                moveX(velocity.x * delta, solidBlocks);
                if (isOnGround) {
                    triggerNormalShake = true;
                    changeState(State.LAND);
                }
                break;
            case JUMP_BACK:
                moveX(velocity.x * delta, solidBlocks);
                if (isOnGround) {
                    triggerNormalShake = true;
                    changeState(State.LAND);
                }
                break;
            case LAND:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= LAND_ANIMATION.totalDuration)
                    changeState(State.IDLE);
                break;
            case STUNNED:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= STUN_DURATION) {
                    changeState(State.STUN_RECOVER);
                }
                break;
            case STUN_RECOVER:
                velocity.x = 0;
                stateTimer += scaledDelta;
                if (stateTimer >= EnemyAnimations.FALSE_KNIGHT_STUN_RECOVER.totalDuration) {
                    triggerPhaseTwo();
                    changeState(State.IDLE);
                }
                break;
            case DEAD:
                velocity.x = 0;
                break;
        }

        moveY(velocity.y * delta, solidBlocks);
    }

    private void decideNextAction(Player player) {
        float distance = Math.abs(player.position.x - position.x);
        ActionType action = chooseNextAction(distance, player);
        facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;
        recordAction(action);

        switch (action) {
            case ATTACK -> changeState(State.ATTACK_ANTICIPATE);
            case RUN -> changeState(State.RUN_ANTICIPATE);
            case NORMAL_JUMP -> {
                pendingJumpType = ActionType.NORMAL_JUMP;
                changeState(State.JUMP_ANTICIPATE);
            }
            case POWER_JUMP -> {
                pendingJumpType = ActionType.POWER_JUMP;
                changeState(State.JUMP_ANTICIPATE);
            }
            case DEFENSIVE_JUMP -> launchDefensiveJump(player);
        }
    }

    private ActionType chooseNextAction(float distance, Player player) {
        float attackWeight, runWeight, jumpWeight, powerJumpWeight = 0f;

        if (distance <= CLOSE_RANGE) {
            attackWeight = 70f;
            runWeight = 10f;
            jumpWeight = 20f;
        } else if (distance >= FAR_RANGE) {
            attackWeight = 5f;
            runWeight = 55f;
            jumpWeight = 40f;
            powerJumpWeight = 25f;
        } else {
            attackWeight = 35f;
            runWeight = 35f;
            jumpWeight = 30f;
            powerJumpWeight = 20f;
        }

        // Restrict power jumps completely if not in Phase Two
        if (!isPhaseTwo) {
            powerJumpWeight = 0f;
        }

        // Do not select a charge when it would immediately pin the player at a wall.
        int runTowardPlayer = player.position.x > position.x ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;
        if (isPlayerCorneredAhead(player, runTowardPlayer) || isNearArenaRunLimit(runTowardPlayer))
            runWeight *= 0.08f;

        // Add fresh random variation every decision so distance bands never become a
        // predictable sequence.
        attackWeight *= randomRange(0.75f, 1.25f);
        runWeight *= randomRange(0.75f, 1.25f);
        jumpWeight *= randomRange(0.75f, 1.25f);
        powerJumpWeight *= randomRange(0.75f, 1.25f);

        attackWeight *= getHistoryMultiplier(ActionType.ATTACK);
        runWeight *= getHistoryMultiplier(ActionType.RUN);
        jumpWeight *= getHistoryMultiplier(ActionType.NORMAL_JUMP);
        powerJumpWeight *= getHistoryMultiplier(ActionType.POWER_JUMP);

        float total = attackWeight + runWeight + jumpWeight + powerJumpWeight;
        if (total <= 0f) {
            // Random fallback still respects phase availability and avoids the immediately
            // previous action.
            List<ActionType> fallbackActions = new ArrayList<>();
            addFallbackAction(fallbackActions, ActionType.ATTACK);
            addFallbackAction(fallbackActions, ActionType.NORMAL_JUMP);
            if (!isNearArenaRunLimit(runTowardPlayer) && !isPlayerCorneredAhead(player, runTowardPlayer))
                addFallbackAction(fallbackActions, ActionType.RUN);
            if (isPhaseTwo)
                addFallbackAction(fallbackActions, ActionType.POWER_JUMP);

            if (fallbackActions.isEmpty())
                fallbackActions.add(ActionType.NORMAL_JUMP);
            return fallbackActions.get(random.nextInt(fallbackActions.size()));
        }

        float roll = random.nextFloat() * total;
        if (roll < attackWeight)
            return ActionType.ATTACK;
        roll -= attackWeight;
        if (roll < runWeight)
            return ActionType.RUN;
        roll -= runWeight;
        if (roll < jumpWeight)
            return ActionType.NORMAL_JUMP;
        return ActionType.POWER_JUMP;
    }

    private void addFallbackAction(List<ActionType> actions, ActionType action) {
        if (recentActions.peekFirst() != action)
            actions.add(action);
    }

    private void recordAction(ActionType action) {
        if (action == lastAction) {
            consecutiveActionCount++;
        } else {
            lastAction = action;
            consecutiveActionCount = 1;
        }

        recentActions.addFirst(action);
        while (recentActions.size() > ACTION_HISTORY_SIZE)
            recentActions.removeLast();
    }

    private float getHistoryMultiplier(ActionType action) {
        if (action == lastAction) {
            if (consecutiveActionCount >= MAX_CONSECUTIVE_ACTIONS)
                return 0f;
            return IMMEDIATE_REPEAT_WEIGHT_MULTIPLIER;
        }

        int historyIndex = 0;
        for (ActionType recentAction : recentActions) {
            if (recentAction == action) {
                if (historyIndex == 1)
                    return 0.45f; // Strongly discourage A-B-A loops.
                return 0.7f;
            }
            historyIndex++;
        }
        return 1f;
    }

    private float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private boolean canTriggerDefensiveJump() {
        if (recentDamageTaken < DAMAGE_BURST_THRESHOLD || defensiveJumpCooldown > 0f)
            return false;
        if (defensiveJumpReactionTimer < 0f) {
            defensiveJumpReactionTimer = randomRange(DEFENSIVE_REACTION_MIN_DELAY,
                    DEFENSIVE_REACTION_MAX_DELAY);
            return false;
        }
        if (defensiveJumpReactionTimer > 0f)
            return false;
        if (lastAction == ActionType.DEFENSIVE_JUMP)
            return false;
        return currentState != State.JUMP_ANTICIPATE
                && currentState != State.NORMAL_JUMP
                && currentState != State.POWER_JUMP_ATTACK
                && currentState != State.JUMP_BACK
                && currentState != State.LAND
                && currentState != State.DEAD
                && currentState != State.STUNNED
                && currentState != State.STUN_RECOVER;
    }

    private void launchPowerJump(Player player) {
        facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;
        velocity.y = POWER_JUMP_SPEED_Y;

        // No X movement: knight jumps straight up in place
        velocity.x = 0;

        isOnGround = false;
        changeState(State.POWER_JUMP_ATTACK);
    }

    private void launchNormalJump(Player player) {
        velocity.y = JUMP_ATTACK_SPEED_Y;

        // Keep the jump aimed at the player, but vary the landing target so repeated
        // jumps never trace the same perfectly predictable arc.
        float targetX = player.position.x + randomRange(-JUMP_TARGET_RANDOM_OFFSET, JUMP_TARGET_RANDOM_OFFSET);
        float dx = targetX - position.x;
        float timeInAir = Math.abs(2 * JUMP_ATTACK_SPEED_Y / Constants.GRAVITY);

        if (timeInAir > 0.1f) {
            velocity.x = dx / timeInAir;
        } else {
            velocity.x = JUMP_ATTACK_SPEED_X * (dx > 0 ? 1 : -1);
        }

        facingDirection = velocity.x > 0 ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;
        isOnGround = false;
        changeState(State.NORMAL_JUMP);
    }

    private void launchDefensiveJump(Player player) {
        recentDamageTaken = 0;
        recentDamageTimer = 0;
        defensiveJumpReactionTimer = -1f;
        defensiveJumpCooldown = DEFENSIVE_JUMP_COOLDOWN;
        recordAction(ActionType.DEFENSIVE_JUMP);

        // Explicitly forces jump in opposite X direction of player
        int awayDirection = player.position.x > position.x ? Constants.LEFT_DIRECTION : Constants.RIGHT_DIRECTION;
        facingDirection = -awayDirection;
        velocity.x = DEFENSIVE_JUMP_SPEED_X * awayDirection;
        velocity.y = DEFENSIVE_JUMP_SPEED_Y;
        isOnGround = false;
        changeState(State.JUMP_BACK);
    }

    private boolean wouldCrossArenaRunLimit(float amount) {
        if (!hasArenaLimits)
            return false;

        float nextX = position.x + amount;
        float safeMinX = arenaMinX + RUN_WALL_SAFETY_MARGIN;
        float safeMaxX = arenaMaxX - FALSE_KNIGHT_HITBOX_WIDTH - RUN_WALL_SAFETY_MARGIN;
        return nextX < safeMinX || nextX > safeMaxX;
    }

    private boolean isNearArenaRunLimit(int direction) {
        if (!hasArenaLimits)
            return false;

        float safeMinX = arenaMinX + RUN_WALL_SAFETY_MARGIN;
        float safeMaxX = arenaMaxX - FALSE_KNIGHT_HITBOX_WIDTH - RUN_WALL_SAFETY_MARGIN;
        if (direction == Constants.RIGHT_DIRECTION)
            return position.x >= safeMaxX - CLOSE_RANGE;
        return position.x <= safeMinX + CLOSE_RANGE;
    }

    private boolean isPlayerCorneredAhead(Player player, int direction) {
        if (!hasArenaLimits)
            return false;

        Rectangle playerBounds = player.getBounds();
        if (direction == Constants.RIGHT_DIRECTION) {
            float distanceFromRightWall = arenaMaxX - (playerBounds.x + playerBounds.width);
            return distanceFromRightWall <= RUN_CORNER_PLAYER_MARGIN;
        }

        float distanceFromLeftWall = playerBounds.x - arenaMinX;
        return distanceFromLeftWall <= RUN_CORNER_PLAYER_MARGIN;
    }

    private boolean hasPlayerPassedBehind(Player player, int direction) {
        if (direction == Constants.RIGHT_DIRECTION)
            return player.position.x + player.getBounds().width < position.x;
        return player.position.x > position.x + FALSE_KNIGHT_HITBOX_WIDTH;
    }

    public void changeState(State newState) {
        currentState = newState;
        stateTimer = 0f;
        switch (newState) {
            case IDLE -> {
                idleDecisionDelay = currentIdleThinkTime * randomRange(0.75f, 1.35f);
                setAnimation(IDLE_ANIMATION);
            }
            case ATTACK_ANTICIPATE -> setAnimation(ATTACK_ANTICIPATE_ANIMATION);
            case ATTACK -> setAnimation(ATTACK_ANIMATION);
            case ATTACK_RECOVER -> setAnimation(ATTACK_RECOVER_ANIMATION);
            case RUN_ANTICIPATE -> setAnimation(RUN_ANTICIPATE_ANIMATION);
            case RUN -> {
                runStartX = position.x;
                runDirection = facingDirection;
                setAnimation(RUN_ANIMATION);
            }
            case JUMP_ANTICIPATE -> setAnimation(JUMP_ANTICIPATE_ANIMATION);
            case NORMAL_JUMP -> setAnimation(JUMP_ANIMATION);
            case JUMP_BACK -> setAnimation(JUMP_ANIMATION);
            case LAND -> setAnimation(LAND_ANIMATION);
            case DEAD -> setAnimation(DEATH_ANIMATION);
            case POWER_JUMP_ATTACK -> setAnimation(JUMP_ATTACK_ANIMATION);
            case STUNNED -> {
                knockbackTimer = 0; // Clear residual knockback momentum
                setAnimation(DEATH_ANIMATION);
            }
            case STUN_RECOVER -> setAnimation(EnemyAnimations.FALSE_KNIGHT_STUN_RECOVER);
        }
    }

    private void setAnimation(EnemyAnimations newAnimation) {
        if (animation != newAnimation) {
            animation = newAnimation;
            animationTime = 0;
        }
    }

    /**
     * Changes all animation frame durations and total durations by a constant
     * factor.
     * Since Enum fields are final in Java, we achieve this by scaling
     * the time increment (delta) for animations and state timers.
     */
    private void applyAnimationSpeedFactor(float factor) {
        this.timeScale = 1.0f / factor;
    }

    private void triggerPhaseTwo() {
        isPhaseTwo = true;
        // Speeds up animations logic by *0.8
        applyAnimationSpeedFactor(0.8f);
        currentRunSpeed = RUN_SPEED * 1.25f; // Faster running
        currentIdleThinkTime = IDLE_THINK_TIME * 0.8f; // Faster decision making
    }

    @Override
    public void kill() {
        super.kill();
        isDead = true;
        velocity.set(0, 0);
        changeState(State.DEAD);
    }

    @Override
    public void takeDamage(int damage, float sourceX, boolean knockback, float knockbackMultiplier) {
        if (isDead)
            return;

        if (currentState == State.STUNNED) {
            int maxAllowedDamage = (FALSE_KNIGHT_HP / 4) - damageTakenDuringStun;
            if (maxAllowedDamage <= 0)
                return; // Prevent any more damage past the 1/4 cap

            int actualDamage = Math.min(damage, maxAllowedDamage);

            // Stun mode grants immunity to death
            if (this.hp - actualDamage <= 0) {
                actualDamage = this.hp - 1;
            }

            if (actualDamage <= 0)
                return;

            super.takeDamage(actualDamage, sourceX, false, knockbackMultiplier);
            damageTakenDuringStun += actualDamage;

            // Switch animation and ALWAYS reset animation timer as requested
            animation = EnemyAnimations.FALSE_KNIGHT_DEATH_HIT;
            animationTime = 0;
            return;
        }

        float reducedKnockbackMultiplier = knockbackMultiplier * FALSE_KNIGHT_KNOCKBACK_MULTIPLIER;
        super.takeDamage(damage, sourceX, knockback, reducedKnockbackMultiplier);
        if (knockback)
            knockbackTimer *= FALSE_KNIGHT_KNOCKBACK_DURATION_SCALE;

        if (!hasBeenStunned && this.hp <= FALSE_KNIGHT_HP / 2) {
            hasBeenStunned = true;
            changeState(State.STUNNED);
            return;
        }

        recentDamageTaken += damage;
        recentDamageTimer = DAMAGE_BURST_WINDOW;
    }

    @Override
    public int getCollisionDamage() {
        return (currentState == State.POWER_JUMP_ATTACK) ? JUMP_ATTACK_DAMAGE : BODY_CONTACT_DAMAGE;
    }

    public Rectangle getActiveAttackHitbox() {
        if (currentState != State.ATTACK || isDead)
            return null;

        float totalDur = animation.totalDuration;
        float ratio = animationTime / totalDur;

        if (ratio <= 0.333f) {
            return new Rectangle(position.x, position.y + Constants.FALSE_KNIGHT_HITBOX_HEIGHT,
                    Constants.FALSE_KNIGHT_HITBOX_WIDTH, 220);
        } else {
            float hitboxWidth = ratio <= 0.666f ? 320 : 280;
            float hitboxHeight = Constants.FALSE_KNIGHT_HITBOX_HEIGHT / 2;
            float x = (facingDirection == Constants.RIGHT_DIRECTION)
                    ? position.x + FALSE_KNIGHT_HITBOX_WIDTH
                    : position.x - hitboxWidth;
            float y = (ratio <= 0.666f) ? position.y + Constants.FALSE_KNIGHT_HITBOX_HEIGHT / 2 : position.y;
            return new Rectangle(x, y, hitboxWidth, hitboxHeight);
        }
    }

    @Override
    public Rectangle getBounds() {
        int diffx = 0;
        int diffy = 0;
        if (currentState == State.DEAD || currentState == State.STUNNED) {
            diffx = 82;
            diffy = -140;
        }
        Rectangle res = new Rectangle(position.x, position.y,
                FALSE_KNIGHT_HITBOX_WIDTH + diffx, FALSE_KNIGHT_HITBOX_HEIGHT + diffy);
        return res;
    }

    // --- NESTED SHOCKWAVE CLASS ---
    public static class Shockwave {
        public Rectangle bounds;
        public float velocityX;
        private int direction;
        public int damage;
        public float maxLifetime = 2.5f;
        public float lifetime = 0f;

        public Shockwave(float startX, float startY, int direction) {
            // Start slightly smaller to match the squat first frame
            bounds = new Rectangle(startX, startY, 40, 40);
            velocityX = 600f * direction;
            this.direction = direction;
            damage = 1;
        }

        public void update(float delta) {
            lifetime += delta;
            bounds.x += velocityX * delta;

            float widthGrowSpeed;
            float heightGrowSpeed;

            // Phase 1: The wave rises rapidly (0.0s to ~0.8s)
            if (lifetime <= 0.8f) {
                widthGrowSpeed = 100f; // Grows forward moderately
                heightGrowSpeed = 180f; // Shoots up rapidly to peak height
            }
            // Phase 2: The wave dissipates and lowers (0.8s to 2.5s)
            else {
                widthGrowSpeed = 20f; // Horizontal growth slows down (stretches out a bit)
                heightGrowSpeed = -50f; // Height shrinks down as it loses energy
            }

            bounds.width += widthGrowSpeed * delta;
            bounds.height += heightGrowSpeed * delta;

            // Clamp height so it doesn't invert or shrink below a minimum size as it fades
            bounds.height = Math.max(bounds.height, 40f);

            // Compensate X growth if traveling left so the front leading edge remains
            // smooth
            if (velocityX < 0) {
                bounds.x -= widthGrowSpeed * delta;
            }

            // Grows stronger
            if (lifetime > 1.5f) {
                damage = FALSE_KNIGHT_ATTACK_DAMAGE + 1;
            } else if (lifetime > 0.75f) {
                damage = FALSE_KNIGHT_ATTACK_DAMAGE;
            }
        }

        public int getDir() {
            return direction;
        }
    }
}
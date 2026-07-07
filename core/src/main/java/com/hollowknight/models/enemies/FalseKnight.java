package com.hollowknight.models.enemies;

import java.util.ArrayList;
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
    public static final int FALSE_KNIGHT_HP = 400;
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
        DEAD
    }

    private enum ActionType {
        ATTACK, RUN, NORMAL_JUMP, POWER_JUMP
    }

    public State currentState = State.IDLE;

    // Timers & Variables
    private float stateTimer = 0f;
    private final Random random = new Random();
    private ActionType lastAction = null;
    private int lastActionRepeatCount = 0;
    private int recentDamageTaken = 0;
    private float recentDamageTimer = 0f;

    public boolean triggerShockwave = false;
    private boolean isPhaseTwo = false;
    private ActionType pendingJumpType = null;

    // Shockwave Tracker
    public List<Shockwave> shockwaves = new ArrayList<>();

    // Tunables
    private static final float POWER_JUMP_SPEED_Y = 900f;
    private static final float IDLE_THINK_TIME = 0.4f;
    private static final float CLOSE_RANGE = 140f;
    private static final float FAR_RANGE = 400f;
    private static final float RUN_SPEED = 220f;
    private static final float RUN_MAX_DURATION = 4f;

    private static final float JUMP_ATTACK_SPEED_X = 260f;
    private static final float JUMP_ATTACK_SPEED_Y = 800f;

    private static final float DEFENSIVE_JUMP_SPEED_X = 300f;
    private static final float DEFENSIVE_JUMP_SPEED_Y = 750f;

    private static final int BODY_CONTACT_DAMAGE = 4;
    private static final int JUMP_ATTACK_DAMAGE = 6;
    private static final float DAMAGE_BURST_WINDOW = 2.5f;
    private static final int DAMAGE_BURST_THRESHOLD = 25;
    private static final int MAX_CONSECUTIVE_REPEATS = 2;

    public FalseKnight(Vector2 pos) {
        super(pos);
        this.velocity = new Vector2(0, Constants.GRAVITY);
        changeState(State.IDLE);
        this.hp = FALSE_KNIGHT_HP;
        this.facingDirection = Constants.LEFT_DIRECTION;
    }

    public static FalseKnight newEnemy(Vector2 pos) {
        return new FalseKnight(pos);
    }

    @Override
    public void respawn() {
        if (isDead)
            return;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        animationTime += delta;

        // Update active shockwaves
        Iterator<Shockwave> iter = shockwaves.iterator();
        while (iter.hasNext()) {
            Shockwave wave = iter.next();
            wave.update(delta);
            if (wave.lifetime > wave.maxLifetime) {
                iter.remove();
            }
        }

        if (recentDamageTimer > 0) {
            recentDamageTimer -= delta;
            if (recentDamageTimer <= 0) {
                recentDamageTimer = 0;
                recentDamageTaken = 0;
            }
        }

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
                stateTimer += delta;
                if (stateTimer >= IDLE_THINK_TIME)
                    decideNextAction(player);
                break;
            case ATTACK_ANTICIPATE:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= ATTACK_ANTICIPATE_ANIMATION.totalDuration)
                    changeState(State.ATTACK);
                break;
            case ATTACK:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= ATTACK_ANIMATION.totalDuration)
                    changeState(State.ATTACK_RECOVER);
                break;
            case ATTACK_RECOVER:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= ATTACK_RECOVER_ANIMATION.totalDuration)
                    changeState(State.IDLE);
                break;
            case RUN_ANTICIPATE:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= RUN_ANTICIPATE_ANIMATION.totalDuration)
                    changeState(State.RUN);
                break;
            case RUN:
                stateTimer += delta;
                facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;
                velocity.x = RUN_SPEED * facingDirection;
                boolean hitWall = moveX(velocity.x * delta, solidBlocks);
                float distanceToPlayer = Math.abs(player.position.x - position.x);
                if (hitWall || distanceToPlayer <= CLOSE_RANGE || stateTimer >= RUN_MAX_DURATION) {
                    changeState(State.IDLE);
                }
                break;
            case JUMP_ANTICIPATE:
                velocity.x = 0;
                stateTimer += delta;
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
                if (isOnGround)
                    changeState(State.LAND);
                break;
            case JUMP_BACK:
                moveX(velocity.x * delta, solidBlocks);
                if (isOnGround)
                    changeState(State.LAND);
                break;
            case LAND:
                velocity.x = 0;
                stateTimer += delta;
                if (stateTimer >= LAND_ANIMATION.totalDuration)
                    changeState(State.IDLE);
                break;
            case DEAD:
                velocity.x = 0;
                break;
        }

        moveY(velocity.y * delta, solidBlocks);
    }

    private void decideNextAction(Player player) {
        float distance = Math.abs(player.position.x - position.x);
        ActionType action = chooseNextAction(distance);
        facingDirection = player.position.x > position.x ? Constants.RIGHT_DIRECTION : Constants.LEFT_DIRECTION;

        if (action == lastAction)
            lastActionRepeatCount++;
        else {
            lastAction = action;
            lastActionRepeatCount = 1;
        }

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
        }
    }

    private ActionType chooseNextAction(float distance) {
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

        if (lastAction != null) {
            float penalty = (lastActionRepeatCount >= MAX_CONSECUTIVE_REPEATS) ? 0f : 0.35f;
            switch (lastAction) {
                case ATTACK -> attackWeight *= penalty;
                case RUN -> runWeight *= penalty;
                case NORMAL_JUMP -> jumpWeight *= penalty;
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
            return ActionType.NORMAL_JUMP;
        return ActionType.POWER_JUMP;
    }

    private boolean canTriggerDefensiveJump() {
        if (recentDamageTaken < DAMAGE_BURST_THRESHOLD)
            return false;
        return currentState != State.JUMP_ANTICIPATE
                && currentState != State.NORMAL_JUMP
                && currentState != State.POWER_JUMP_ATTACK
                && currentState != State.JUMP_BACK
                && currentState != State.LAND
                && currentState != State.DEAD;
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

        // Exact targeting using physics calculation so he lands directly on the player
        float dx = player.position.x - position.x;
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

        // Explicitly forces jump in opposite X direction of player
        int awayDirection = player.position.x > position.x ? Constants.LEFT_DIRECTION : Constants.RIGHT_DIRECTION;
        facingDirection = -awayDirection;
        velocity.x = DEFENSIVE_JUMP_SPEED_X * awayDirection;
        velocity.y = DEFENSIVE_JUMP_SPEED_Y;
        isOnGround = false;
        changeState(State.JUMP_BACK);
    }

    private void changeState(State newState) {
        currentState = newState;
        stateTimer = 0f;
        switch (newState) {
            case IDLE -> setAnimation(IDLE_ANIMATION);
            case ATTACK_ANTICIPATE -> setAnimation(ATTACK_ANTICIPATE_ANIMATION);
            case ATTACK -> setAnimation(ATTACK_ANIMATION);
            case ATTACK_RECOVER -> setAnimation(ATTACK_RECOVER_ANIMATION);
            case RUN_ANTICIPATE -> setAnimation(RUN_ANTICIPATE_ANIMATION);
            case RUN -> setAnimation(RUN_ANIMATION);
            case JUMP_ANTICIPATE -> setAnimation(JUMP_ANTICIPATE_ANIMATION);
            case NORMAL_JUMP -> setAnimation(JUMP_ANIMATION);
            case JUMP_BACK -> setAnimation(JUMP_ANIMATION);
            case LAND -> setAnimation(LAND_ANIMATION);
            case DEAD -> setAnimation(DEATH_ANIMATION);
            case POWER_JUMP_ATTACK -> setAnimation(JUMP_ATTACK_ANIMATION);
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
        if (!isPhaseTwo && hp <= FALSE_KNIGHT_HP / 2)
            isPhaseTwo = true;
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
                    Constants.FALSE_KNIGHT_HITBOX_WIDTH, 190);
        } else {
            float hitboxWidth = 250;
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
        if (currentState == State.DEAD) {
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
            bounds = new Rectangle(startX, startY, 40, 40);
            velocityX = 600f * direction;
            this.direction = direction;
            damage = 1;
        }

        public void update(float delta) {
            lifetime += delta;
            bounds.x += velocityX * delta;

            // Grows larger as it travels
            float growSpeed = 70f;
            bounds.width += growSpeed * delta;
            bounds.height += growSpeed * delta;

            // Compensate X growth if traveling left so the front continues correctly
            if (velocityX < 0) {
                bounds.x -= growSpeed * delta;
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
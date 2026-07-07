package com.hollowknight.models.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.enemies.Enemy;
import com.hollowknight.models.player.PlayerEffectAnimation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerProjectile {
    public Vector2 position;
    public float startX;
    public int direction;

    public boolean isExploding = false;
    public boolean isFinished = false;
    public float animationTime = 0;

    // Tracks enemies hit so we don't apply damage every single frame
    private Set<Enemy> hitEnemies = new HashSet<>();

    public PlayerProjectile(Vector2 startPos, int direction) {
        this.position = new Vector2(startPos);
        // Spawn slightly in front of the player
        this.position.x += direction * (Constants.PLAYER_HITBOX_WIDTH + 10f);
        this.startX = this.position.x;
        this.direction = direction;
    }

    public void update(float delta, List<Rectangle> solidBlocks, List<Enemy> enemies) {
        animationTime += delta;

        // If it hit something, wait for the explosion animation to finish before
        // destroying
        if (isExploding) {
            if (animationTime >= PlayerEffectAnimation.SOUL_BALL_END.duration) {
                isFinished = true;
            }
            return;
        }

        // Travel horizontally
        position.x += direction * Constants.PROJECTILE_SPEED * delta;
        Rectangle bounds = getBounds();

        // 1. Check Max Range
        if (Math.abs(position.x - startX) >= Constants.PROJECTILE_MAX_RANGE) {
            explode();
            return;
        }

        // 2. Check Wall Collisions
        for (Rectangle solid : solidBlocks) {
            if (bounds.overlaps(solid)) {
                explode();
                return;
            }
        }

        // 3. Check Enemy Collisions (Piercing)
        for (Enemy enemy : enemies) {
            if (enemy.isDead || hitEnemies.contains(enemy))
                continue;

            if (bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(Constants.PROJECTILE_DAMAGE, position.x, true);
                hitEnemies.add(enemy);
            }
        }
    }

    private void explode() {
        isExploding = true;
        animationTime = 0; // Reset timer for the SOUL_BALL_END explosion effect
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, Constants.PROJECTILE_SIZE, Constants.PROJECTILE_SIZE);
    }
}
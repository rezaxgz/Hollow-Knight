package com.hollowknight.models;

import com.badlogic.gdx.math.Vector2;
import com.hollowknight.views.AnimationType;

public class Player {
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public boolean isOnGround = false;
    public boolean movingLeft = false, movingRight = false;
    public AnimationType currentAnimation = AnimationType.HOLLOW_KNIGHT_IDLE;
    public float stateTime = 0;

    public void update(float delta) {
        if (position.y <= 0.001)
            isOnGround = true;
        else
            isOnGround = false;

        if (!isOnGround) {
            velocity.y -= 1000 * delta;
        } else if (velocity.y < 0.01) {
            velocity.y = 0;
            position.y = 0;
        }

        if (movingLeft) {
            velocity.x = -500;
            currentAnimation = AnimationType.HOLLOW_KNIGHT_WALK;
        } else if (movingRight) {
            velocity.x = 500;
            currentAnimation = AnimationType.HOLLOW_KNIGHT_WALK;
        } else {
            velocity.x = 0;
            currentAnimation = AnimationType.HOLLOW_KNIGHT_IDLE;
        }

        position.add(velocity.cpy().scl(delta));

        stateTime += delta;
    }

    public void jump() {
        if (isOnGround) {
            velocity.y = 500;
        }
    }
}

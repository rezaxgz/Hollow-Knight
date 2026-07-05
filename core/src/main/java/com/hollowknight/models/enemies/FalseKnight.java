package com.hollowknight.models.enemies;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.player.Player;

public class FalseKnight extends Enemy {

    public FalseKnight(Vector2 pos) {
        super(pos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public Rectangle getBounds() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBounds'");
    }

}

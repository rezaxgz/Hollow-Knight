package com.hollowknight.models;

public class Game {
    public Player player = new Player();

    public void update(float delta) {
        player.update(delta);
    }
}

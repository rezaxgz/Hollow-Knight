package com.hollowknight.models;

public class Game {
    private String worldName = "new world";
    public Player player = new Player();

    public void update(float delta) {
        player.update(delta);
    }

    public String getWorldName() {
        return worldName;
    }

}

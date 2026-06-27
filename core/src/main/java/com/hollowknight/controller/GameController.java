package com.hollowknight.controller;

import com.hollowknight.models.world.GameWorld;

public class GameController {
    GameWorld world;
    PlayerController playerController;

    public GameController(GameWorld world) {
        this.world = world;
        this.playerController = new PlayerController(world.player);
    }

    public void update(float delta) {
        world.update(delta);
    }
}

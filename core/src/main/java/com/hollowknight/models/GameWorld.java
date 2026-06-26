package com.hollowknight.models;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.hollowknight.models.gamedata.GameSave;

public class GameWorld {
    private String worldName = "new world";
    public TiledMap map;
    public Player player = new Player();

    public GameWorld(GameSave save) {
        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load(save.gameLevel.tmxPath);
        player = save.player;
    }

    public void update(float delta) {
        player.update(delta);
    }

    public String getWorldName() {
        return worldName;
    }

}

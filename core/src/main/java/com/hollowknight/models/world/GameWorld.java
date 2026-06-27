package com.hollowknight.models.world;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.player.Player;

public class GameWorld {
    private String worldName = "new world";
    public TiledMap map;
    public Player player = new Player();
    private List<Rectangle> solidBlocks = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<>();

    public GameWorld(GameSave save) {
        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load(save.gameLevel.tmxPath);
        player = save.player;

        MapLayer solids = map.getLayers().get("Solid");
        for (MapObject obj : solids.getObjects()) {
            if (!(obj instanceof RectangleMapObject))
                continue;
            Rectangle rect = ((RectangleMapObject) (obj)).getRectangle();
            solidBlocks.add(rect);
        }

        MapLayer obsticleLayer = map.getLayers().get("Obstacles");
        for (MapObject obj : obsticleLayer.getObjects()) {
            if (!(obj instanceof RectangleMapObject))
                continue;
            Rectangle rect = ((RectangleMapObject) (obj)).getRectangle();
            boolean isInstantDeath = (boolean) obj.getProperties().get("isInstantDeath");
            obstacles.add(new Obstacle(rect, isInstantDeath));
        }
    }

    public void update(float delta) {
        player.update(delta, solidBlocks);
    }

    public String getWorldName() {
        return worldName;
    }

}

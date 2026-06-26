package com.hollowknight.models.enums;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;

public enum GameLevel {
    FORGOTTEN_CROSSROADS("map/forgotten_crossroads.tmx");

    public String tmxPath;

    GameLevel(String tmxPaString) {
        this.tmxPath = tmxPaString;
    }

    public Vector2 getSpawnPoint() {
        TmxMapLoader loader = new TmxMapLoader();
        TiledMap map = loader.load(tmxPath);
        MapLayer layer = map.getLayers().get("Solid");

        for (MapObject object : layer.getObjects()) {
            if ("Spawn Point".equals(object.getName())) {
                PointMapObject pointMapObject = (PointMapObject) object;
                return new Vector2(pointMapObject.getPoint().x, pointMapObject.getPoint().y);
            }
        }

        return new Vector2();
    }
}

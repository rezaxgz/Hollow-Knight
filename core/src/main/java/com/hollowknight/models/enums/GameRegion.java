package com.hollowknight.models.enums;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.views.GameAssetManager;

public enum GameRegion {
    FORGOTTEN_CROSSROADS("ForgottenCrossroads", "Forgotten Crossroads", GameAssetManager.crossRoadsMusic),
    GREENPATH("Greenpath", "Greenpath", GameAssetManager.greenPathMusic),
    BOSS_ROOM("BossRoom", "Boss Room", GameAssetManager.falseKnightMusic);

    public final String id; // Matches the Tiled MapObject name
    public final String displayName; // Used for UI menus
    public final Music music; // Background music for the regionq

    public static final String MAP_PATH = "map/forgotten_crossroads.tmx";

    GameRegion(String id, String displayName, Music music) {
        this.id = id;
        this.displayName = displayName;
        this.music = music;
    }

    public static GameRegion fromId(String id) {
        if (id == null)
            return FORGOTTEN_CROSSROADS;
        for (GameRegion region : values()) {
            if (region.id.equalsIgnoreCase(id)) {
                return region;
            }
        }
        return FORGOTTEN_CROSSROADS; // Default fallback
    }

    public static Vector2 getSpawnPoint() {
        TmxMapLoader loader = new TmxMapLoader();
        TiledMap map = loader.load(MAP_PATH);
        MapLayer layer = map.getLayers().get("Solid");

        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if ("Spawn Point".equals(object.getName()) && object instanceof PointMapObject) {
                    PointMapObject pointMapObject = (PointMapObject) object;
                    return new Vector2(pointMapObject.getPoint().x, pointMapObject.getPoint().y);
                }
            }
        }
        return new Vector2();
    }
}
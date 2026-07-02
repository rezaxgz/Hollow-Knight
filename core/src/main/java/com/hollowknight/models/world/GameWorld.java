package com.hollowknight.models.world;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.Constants;
import com.hollowknight.models.enemies.Enemy;
import com.hollowknight.models.enemies.GroundEnemy;
import com.hollowknight.models.enemies.GroundEnemyType;
import com.hollowknight.models.enemies.HuskHornHead;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.player.Player;
import com.hollowknight.models.settings.GameCheat;

public class GameWorld {
    private String worldName = "new world";
    public TiledMap map;
    public Player player = new Player();
    private List<Rectangle> solidBlocks = new ArrayList<>();
    private List<Hazard> hazards = new ArrayList<>();

    public List<Enemy> enemies = new ArrayList<>();

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

        MapLayer obsticleLayer = map.getLayers().get("Hazards");
        for (MapObject obj : obsticleLayer.getObjects()) {
            if (!(obj instanceof RectangleMapObject))
                continue;
            Rectangle rect = ((RectangleMapObject) (obj)).getRectangle();
            Object death = obj.getProperties().get("isInstantDeath");
            boolean isInstantDeath = death == null ? false : (boolean) death;
            hazards.add(new Hazard(rect, isInstantDeath));
        }

        MapLayer groundEnemySpawnPoints = map.getLayers().get("GroundEnemySpawnPoints");
        for (MapObject obj : groundEnemySpawnPoints.getObjects()) {
            if (!(obj instanceof PointMapObject))
                continue;
            Vector2 point = ((PointMapObject) obj).getPoint();
            int type = (int) obj.getProperties().get("type");
            GroundEnemyType enemytype = GroundEnemyType.fromInt(type);

            enemies.add(GroundEnemy.newEnemy(enemytype, point));
        }

        MapLayer hornheadSpawnPoints = map.getLayers().get("HornheadSpawnPoints");
        for (MapObject obj : hornheadSpawnPoints.getObjects()) {
            if (!(obj instanceof PointMapObject))
                continue;
            Vector2 point = ((PointMapObject) obj).getPoint();

            enemies.add(HuskHornHead.newEnemy(point));
        }

    }

    public void update(float delta) {
        player.update(delta, solidBlocks);
        checkHazards();

        updateEnemies(delta);

        checkEnemyCollisions();
    }

    private void updateEnemies(float delta) {
        // Update enemies
        for (Enemy enemy : enemies) {
            float dist = player.position.dst(enemy.position);
            if (dist >= Constants.ENEMY_IGNORE_RADIUS) {
                continue;
            }
            if (dist >= Constants.ENEMY_RESPAWN_RADIUS) {
                enemy.respawn();
                continue;
            }
            if (player.position.dst(enemy.position) <= Constants.ENEMY_ACTIVE_RADIUS) {
                enemy.update(delta, player, solidBlocks);
            }
        }
    }

    private void checkHazards() {
        Rectangle playerBounds = player.getBounds();

        for (Hazard hazard : hazards) {

            if (!playerBounds.overlaps(hazard.getBounds()))
                continue;

            if (hazard.isInstantDeath()) {
                player.kill();
            } else {
                player.takeDamage();
            }

            break;
        }
    }

    private void checkEnemyCollisions() {
        // If the player is already dead or invincible, no need to check collisions
        if (player.getVitals().isDead() || player.isInvinvible()) {
            return;
        }

        Rectangle playerBounds = player.getBounds();

        for (Enemy enemy : enemies) {
            // Ignore dead enemies
            if (enemy.isDead) {
                continue;
            }

            // Only check enemies that are close enough to be active
            if (player.position.dst(enemy.position) > Constants.ENEMY_ACTIVE_RADIUS) {
                continue;
            }

            if (playerBounds.overlaps(enemy.getBounds())) {
                player.takeDamage();
                break; // Exit loop after taking damage once per frame
            }
        }
    }

    public void applyCheat(GameCheat cheat) {
        if (cheat == GameCheat.KILL_ENEMIES) {
            for (Enemy enemy : enemies) {
                if (player.position.dst(enemy.position) <= Constants.ENEMY_ACTIVE_RADIUS) {
                    enemy.kill();
                }
            }
        } else {
            player.applyCheat(cheat);
        }
    }

    public String getWorldName() {
        return worldName;
    }

}

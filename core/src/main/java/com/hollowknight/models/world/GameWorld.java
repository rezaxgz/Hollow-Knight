package com.hollowknight.models.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.hollowknight.models.enemies.EnemyFactory;
import com.hollowknight.models.enemies.EnemyType;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.player.Player;
import com.hollowknight.models.player.states.CombatState;
import com.hollowknight.models.settings.GameCheat;

public class GameWorld {
    private String worldName = "new world";
    public TiledMap map;
    public Player player;
    private List<Rectangle> solidBlocks = new ArrayList<>();
    private List<Hazard> hazards = new ArrayList<>();

    public List<Enemy> enemies = new ArrayList<>();

    private Set<Enemy> enemiesHitThisAttack = new HashSet<>();

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

        MapLayer groundEnemySpawnPoints = map.getLayers().get("EnemySpawnPoints");
        for (MapObject obj : groundEnemySpawnPoints.getObjects()) {
            if (!(obj instanceof PointMapObject))
                continue;
            Vector2 point = ((PointMapObject) obj).getPoint();
            Object typeObj = obj.getProperties().get("type");
            int type = typeObj != null ? (int) typeObj : 0;
            EnemyType enemyType = EnemyType.fromInt(type);

            enemies.add(EnemyFactory.newEnemy(point, enemyType));
        }

    }

    public void update(float delta) {
        player.update(delta, solidBlocks);
        checkHazards();

        updateEnemies(delta);

        checkEnemyCollisions();

        checkPlayerAttacks();
    }

    private void updateEnemies(float delta) {
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
                player.takeDamage(Constants.HAZARD_DAMAGE, hazard.getBounds().x + (hazard.getBounds().width / 2f));
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
                player.takeDamage(enemy.getCollisionDamage(), enemy.position.x);
                break; // Exit loop after taking damage once per frame
            }
        }
    }

    private void checkPlayerAttacks() {
        // If the player is not attacking, clear the tracking set so they can hit
        // enemies again next swing
        if (player.combatState != CombatState.ATTACK) {
            enemiesHitThisAttack.clear();
            return;
        }

        Rectangle attackBounds = player.getAttackHitbox();
        if (attackBounds == null)
            return;

        for (Enemy enemy : enemies) {
            if (enemy.isDead)
                continue;

            // Ignore enemies that are too far away to matter
            if (player.position.dst(enemy.position) > Constants.ENEMY_ACTIVE_RADIUS)
                continue;

            if (attackBounds.overlaps(enemy.getBounds())) {
                // Ensure we only hit this enemy ONCE per swing
                if (!enemiesHitThisAttack.contains(enemy)) {
                    enemy.takeDamage(Constants.PLAYER_SLASH_DAMAGE, player.position.x);
                    enemiesHitThisAttack.add(enemy);
                    player.getVitals().addSouls(Constants.ATTACK_HIT_SOULS_BONUS);

                    // TODO: The classic Hollow Knight "Pogo" bounce!
                    // if (player.animation.name().contains("DOWN_SLASH")) {
                    // player.velocity.y = Constants.JUMP_SPEED;
                    // }
                }
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

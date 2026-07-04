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
import com.hollowknight.models.enemies.CrystalGuardian;
import com.hollowknight.models.enemies.Enemy;
import com.hollowknight.models.enemies.EnemyFactory;
import com.hollowknight.models.enemies.EnemyType;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.npc.Zote;
import com.hollowknight.models.player.Player;
import com.hollowknight.models.player.states.CombatState;
import com.hollowknight.models.settings.GameCheat;

public class GameWorld {
    private String worldName = "new world";
    public TiledMap map;
    public Player player;
    public List<Rectangle> solidBlocks = new ArrayList<>();
    private List<Hazard> hazards = new ArrayList<>();

    public List<Enemy> enemies = new ArrayList<>();

    private Set<Enemy> enemiesHitThisAttack = new HashSet<>();

    public List<PlayerProjectile> projectiles = new ArrayList<>();

    public Zote zote;

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

        MapLayer zoteLayer = map.getLayers().get("Zote");
        if (zoteLayer != null) {
            for (MapObject obj : zoteLayer.getObjects()) {
                if (obj instanceof PointMapObject) {
                    Vector2 point = ((PointMapObject) obj).getPoint();
                    zote = new Zote(point);
                    break; // Assuming only one Zote exists
                }
            }
        }
    }

    public void update(float delta) {
        player.update(delta, solidBlocks);
        checkHazards();

        updateEnemies(delta);

        checkLaserCollisions();
        checkEnemyCollisions();

        checkPlayerAttacks();
        checkSoulScreamAttacks();

        checkSpiritCastAttacks();
        updateProjectiles(delta);

        if (zote != null) {
            zote.update(delta, player);
        }
    }

    private void checkSpiritCastAttacks() {
        if (player.combatState == CombatState.CAST && player.triggerSpiritCast) {
            // Center the projectile vertically with the player's body
            Vector2 spawnPos = new Vector2(player.position.x,
                    player.position.y + Constants.PLAYER_HITBOX_HEIGHT / 2 - Constants.PROJECTILE_SIZE / 2);

            projectiles.add(new PlayerProjectile(spawnPos, player.getDirection()));
            player.triggerSpiritCast = false;
        }
    }

    private void updateProjectiles(float delta) {
        // Iterator safely removes finished projectiles during the loop
        java.util.Iterator<PlayerProjectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            PlayerProjectile proj = iterator.next();
            proj.update(delta, solidBlocks, enemies);

            if (proj.isFinished) {
                iterator.remove();
            }
        }
    }

    private void checkLaserCollisions() {
        // If the player is already dead or invincible, no need to check collisions
        if (player.getVitals().isDead() || player.isInvinvible()) {
            return;
        }

        Rectangle playerBounds = player.getBounds();

        for (Enemy enemy : enemies) {
            // Only the Crystal Guardian has a laser
            if (enemy instanceof CrystalGuardian) {
                CrystalGuardian guardian = (CrystalGuardian) enemy;
                Rectangle laserBounds = guardian.getActiveLaserBounds(solidBlocks);

                // If laserBounds is not null, the laser is currently firing
                if (laserBounds != null && playerBounds.overlaps(laserBounds)) {
                    // Taking damage from laser
                    player.takeDamage(2, guardian.position.x);
                    break; // Exit loop after taking damage once per frame
                }
            }
        }
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
        Rectangle attackBounds = player.combatState == CombatState.ATTACK ? player.getAttackHitbox() : null;
        boolean isDownSlashing = player.animation != null && player.animation.name().contains("DOWN_SLASH");

        for (Hazard hazard : hazards) {

            // Allow a Pogo bounce if attacking downward onto the hazard
            if (isDownSlashing && attackBounds != null && attackBounds.overlaps(hazard.getBounds())) {
                player.pogo();
                continue; // Skip processing damage for this hazard this frame
            }

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

        boolean isDownSlashing = player.animation != null && player.animation.name().contains("DOWN_SLASH");

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

                    if (isDownSlashing) {
                        player.pogo();
                    }
                }
            }
        }
    }

    private void checkSoulScreamAttacks() {
        // Only run this if the player's internal timer fired a tick this frame
        if (player.combatState == CombatState.SCREAM && player.triggerScreamDamage) {

            Rectangle screamHitbox = player.getScreamHitbox();
            if (screamHitbox == null)
                return;

            for (Enemy enemy : enemies) {
                if (enemy.isDead)
                    continue;
                if (player.position.dst(enemy.position) > Constants.ENEMY_ACTIVE_RADIUS)
                    continue;

                if (screamHitbox.overlaps(enemy.getBounds())) {
                    enemy.takeDamage(Constants.SOUL_SCREAM_TICK_DAMAGE, player.position.x);
                }
            }

            // Turn the flag off so we don't double-hit before the next scheduled tick
            player.triggerScreamDamage = false;
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
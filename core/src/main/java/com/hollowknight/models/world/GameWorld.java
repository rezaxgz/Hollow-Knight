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
import com.hollowknight.models.enemies.FalseKnight;
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
    public Rectangle bossRoomBounds;

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
            boolean isInstantDeath = death != null && (boolean) death;
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
                    break;
                }
            }
        }

        MapLayer regionsLayer = map.getLayers().get("Regions");
        if (regionsLayer != null) {
            for (MapObject obj : regionsLayer.getObjects()) {
                if (obj instanceof RectangleMapObject && "BossRoom".equals(obj.getName())) {
                    bossRoomBounds = ((RectangleMapObject) obj).getRectangle();
                    break;
                }
            }
        }
    }

    public void update(float delta) {
        if (Constants.flag)
            return;
        player.update(delta, solidBlocks);
        checkHazards();
        updateEnemies(delta);
        checkLaserCollisions();
        checkEnemyCollisions();
        checkPlayerAttacks();
        checkSoulScreamAttacks();
        checkSpiritCastAttacks();
        updateProjectiles(delta);

        if (zote != null)
            zote.update(delta, player);
    }

    private void checkSpiritCastAttacks() {
        if (player.combatState == CombatState.CAST && player.triggerSpiritCast) {
            Vector2 spawnPos = new Vector2(player.position.x,
                    player.position.y + Constants.PLAYER_HITBOX_HEIGHT / 2 - Constants.PROJECTILE_SIZE / 2);
            projectiles.add(new PlayerProjectile(spawnPos, player.getDirection()));
            player.triggerSpiritCast = false;
        }
    }

    private void updateProjectiles(float delta) {
        java.util.Iterator<PlayerProjectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            PlayerProjectile proj = iterator.next();
            proj.update(delta, solidBlocks, enemies);
            if (proj.isFinished)
                iterator.remove();
        }
    }

    private void checkLaserCollisions() {
        if (player.getVitals().isDead() || player.isInvinvible())
            return;
        Rectangle playerBounds = player.getBounds();
        for (Enemy enemy : enemies) {
            if (enemy instanceof CrystalGuardian) {
                CrystalGuardian guardian = (CrystalGuardian) enemy;
                Rectangle laserBounds = guardian.getActiveLaserBounds(solidBlocks);
                if (laserBounds != null && playerBounds.overlaps(laserBounds)) {
                    player.takeDamage(2, guardian.position.x);
                    break;
                }
            }
        }
    }

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (enemy instanceof FalseKnight) {
                enemy.update(delta, player, solidBlocks);
                continue;
            }
            float dist = player.position.dst(enemy.position);
            if (dist >= Constants.ENEMY_IGNORE_RADIUS)
                continue;
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
            if (isDownSlashing && attackBounds != null && attackBounds.overlaps(hazard.getBounds())) {
                player.pogo();
                continue;
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
        if (player.getVitals().isDead() || player.isInvinvible())
            return;
        Rectangle playerBounds = player.getBounds();

        for (Enemy enemy : enemies) {
            if (enemy.isDead)
                continue;

            // Allow False Knight to strike from far away using expanding shockwaves
            if (!(enemy instanceof FalseKnight)
                    && player.position.dst(enemy.position) > Constants.ENEMY_ACTIVE_RADIUS) {
                continue;
            }

            // Check Boss Specific hitboxes
            if (enemy instanceof FalseKnight) {
                FalseKnight fk = (FalseKnight) enemy;

                // Melee strike
                Rectangle attackHitbox = fk.getActiveAttackHitbox();
                if (attackHitbox != null && playerBounds.overlaps(attackHitbox)) {
                    player.takeDamage(FalseKnight.FALSE_KNIGHT_ATTACK_DAMAGE, fk.position.x);
                    return;
                }

                // Floor Shockwaves
                for (FalseKnight.Shockwave wave : fk.shockwaves) {
                    if (playerBounds.overlaps(wave.bounds)) {
                        player.takeDamage(wave.damage, wave.bounds.x);
                        return;
                    }
                }
            }

            // Standard Body Contact collision
            if (playerBounds.overlaps(enemy.getBounds())) {
                player.takeDamage(enemy.getCollisionDamage(), enemy.position.x);
                break;
            }
        }
    }

    private void checkPlayerAttacks() {
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
            if (player.position.dst(enemy.position) > Constants.ENEMY_ACTIVE_RADIUS)
                continue;

            if (attackBounds.overlaps(enemy.getBounds())) {
                if (!enemiesHitThisAttack.contains(enemy)) {
                    enemy.takeDamage(Constants.PLAYER_SLASH_DAMAGE, player.position.x);
                    enemiesHitThisAttack.add(enemy);
                    player.getVitals().addSouls(Constants.ATTACK_HIT_SOULS_BONUS);
                    if (isDownSlashing)
                        player.pogo();
                }
            }
        }
    }

    private void checkSoulScreamAttacks() {
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
            player.triggerScreamDamage = false;
        }
    }

    public boolean isBossFightActive() {
        if (bossRoomBounds == null)
            return false;
        if (!bossRoomBounds.overlaps(player.getBounds()))
            return false;
        for (Enemy enemy : enemies) {
            if (enemy instanceof FalseKnight && !enemy.isDead)
                return true;
        }
        return false;
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
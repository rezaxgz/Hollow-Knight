package com.hollowknight.models.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.Constants;
import com.hollowknight.models.achievements.AchievementManager;
import com.hollowknight.models.enemies.CrystalGuardian;
import com.hollowknight.models.enemies.Enemy;
import com.hollowknight.models.enemies.EnemyFactory;
import com.hollowknight.models.enemies.EnemyType;
import com.hollowknight.models.enemies.FalseKnight;
import com.hollowknight.models.enums.GameRegion;
import com.hollowknight.models.gamedata.GameSave;
import com.hollowknight.models.npc.Zote;
import com.hollowknight.models.player.Player;
import com.hollowknight.models.player.states.CombatState;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.views.GameAssetManager;

public class GameWorld {

    // --- Environment & State Properties ---
    private String worldName = "new world";
    public TiledMap map;
    public GameRegion currentRegion = GameRegion.FORGOTTEN_CROSSROADS;
    public Map<String, Rectangle> regionBounds = new HashMap<>();
    public List<Rectangle> solidBlocks = new ArrayList<>();
    private List<Hazard> hazards = new ArrayList<>();
    public GameSave saveLoadedFrom;

    // --- Entity Properties ---
    public Player player;
    public List<Enemy> enemies = new ArrayList<>();
    public List<PlayerProjectile> projectiles = new ArrayList<>();
    private Set<Enemy> enemiesHitThisAttack = new HashSet<>();
    public Zote zote;

    // --- Boss & Objective Properties ---
    public Rectangle bossRoomBounds;
    private Rectangle activationZone;
    public Rectangle bossDoor;
    private Rectangle bossRoomRightWall;
    private Vector2 falseKnightSpawnPoint;
    public Rectangle gameEndZone;

    public boolean bossFightActivated = false;
    public boolean bossFightCompleted = false;
    public boolean bossJustDefeated = false;
    public float gateDropProgress = 0.0f;
    private Vector2 playerTPPoint = new Vector2();

    // --- System & Tracker Properties ---
    private int numberOfEnemiesKilled = 0;
    private int numberOfDeaths = 0;
    private float passedTimeBuffer = 0;
    private long totalPassedTime = 0;
    public float cameraShakeTimer = 0f;
    public float cameraShakeIntensity = 0f;

    // --- Initialization ---
    public GameWorld(GameSave save) {
        saveLoadedFrom = save;

        // 1. Load Data
        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load(GameRegion.MAP_PATH);
        player = save.player;
        this.currentRegion = save.currentRegion;
        AchievementManager.getInstance().onRegionChange(save.currentRegion);

        this.totalPassedTime = save.totalPassedTime;
        this.numberOfEnemiesKilled = save.numberOfEnemiesKilled;
        this.numberOfDeaths = save.numberOfDeaths;
        this.bossFightActivated = save.bossFightActivated;
        this.bossFightCompleted = save.bossFightCompleted;

        // 2. Parse Environment Architecture
        MapLayer solids = map.getLayers().get("Solid");
        for (MapObject obj : solids.getObjects()) {
            if (!(obj instanceof RectangleMapObject))
                continue;
            Rectangle rect = ((RectangleMapObject) (obj)).getRectangle();
            solidBlocks.add(rect);

            String objectName = obj.getName();
            if ("BoosRoom RightWall".equals(objectName) || "BossRoom RightWall".equals(objectName)) {
                bossRoomRightWall = rect;
            }
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

        // 3. Parse Regions
        MapLayer regionsLayer = map.getLayers().get("Regions");
        if (regionsLayer != null) {
            for (MapObject obj : regionsLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                    String regionName = obj.getName();

                    if (regionName != null) {
                        regionBounds.put(regionName, rect);
                    }

                    if ("BossRoom".equals(obj.getName())) {
                        bossRoomBounds = rect;
                    }
                }
            }
        }

        // 4. Parse Entities (Enemies, NPCs, Boss Zones)
        MapLayer groundEnemySpawnPoints = map.getLayers().get("EnemySpawnPoints");
        for (MapObject obj : groundEnemySpawnPoints.getObjects()) {
            if (!(obj instanceof PointMapObject))
                continue;
            Vector2 point = ((PointMapObject) obj).getPoint();
            Object typeObj = obj.getProperties().get("type");
            int type = typeObj != null ? (int) typeObj : 0;
            EnemyType enemyType = EnemyType.fromInt(type);

            if (enemyType == EnemyType.FALSE_KNIGHT) {
                falseKnightSpawnPoint = point;
            } else {
                enemies.add(EnemyFactory.newEnemy(point, enemyType));
            }
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

        MapLayer bossLayer = map.getLayers().get("Boss");
        if (bossLayer != null) {
            for (MapObject obj : bossLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                    if ("activationZone".equals(obj.getName())) {
                        activationZone = rect;
                    } else if ("BossDoor".equals(obj.getName())) {
                        bossDoor = rect;
                    } else if ("GameEndZone".equals(obj.getName())) {
                        gameEndZone = rect;
                    }
                } else if (obj instanceof PointMapObject) {
                    Vector2 point = ((PointMapObject) obj).getPoint();
                    if (obj.getName().equals("Player TP")) {
                        playerTPPoint = point.cpy();
                    }
                }
            }
        }

        // 5. Restore Boss State if Active
        if (this.bossFightActivated && !this.bossFightCompleted && this.bossDoor != null) {
            this.solidBlocks.add(this.bossDoor);

            FalseKnight fk = new FalseKnight(new Vector2(save.bossX, save.bossY));
            fk.setHp(save.bossHp);
            configureFalseKnightArena(fk);
            try {
                fk.changeState(FalseKnight.State.valueOf(save.bossState));
            } catch (Exception e) {
                fk.changeState(FalseKnight.State.IDLE);
            }
            this.enemies.add(fk);
        }

        AudioController.getInstance().playBgm(currentRegion.music);
    }

    // --- Core Update Sequence ---
    public void update(float delta) {
        AudioController.getInstance().update(delta);
        updateTimers(delta);
        manageCameraShakes();
        updateCurrentRegion();
        updateBossFight(delta);

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
            zote.update(delta, player, solidBlocks);

        registerDeaths();
    }

    // --- Environment & System Management ---
    private void updateTimers(float delta) {
        passedTimeBuffer += delta;
        if (cameraShakeTimer > 0) {
            cameraShakeTimer -= delta;
            if (cameraShakeTimer <= 0)
                cameraShakeIntensity = 0f;
        }
        if (passedTimeBuffer >= 1f) {
            passedTimeBuffer -= 1f;
            totalPassedTime += 1;
        }
    }

    private void updateCurrentRegion() {
        Rectangle playerBounds = player.getBounds();
        for (Map.Entry<String, Rectangle> entry : regionBounds.entrySet()) {
            if (entry.getValue().overlaps(playerBounds)) {
                GameRegion newRegion = GameRegion.fromId(entry.getKey());
                if (this.currentRegion != newRegion) {
                    AudioController.getInstance().transitionBgm(newRegion.music, 1.5f);
                    this.currentRegion = newRegion;
                    AchievementManager.getInstance().onRegionChange(newRegion);
                }
                break;
            }
        }
    }

    private void manageCameraShakes() {
        if (player.triggerDamageShake) {
            triggerShake(3f, 0.2f);
            player.triggerDamageShake = false;
        }
        if (player.triggerSpellShake) {
            triggerShake(3f,
                    player.combatState == CombatState.SCREAM ? Constants.SOUL_SCREAM_TIME : 0.2f);
            player.triggerSpellShake = false;
        }
        for (Enemy enemy : enemies) {
            if (enemy instanceof FalseKnight) {
                FalseKnight fk = (FalseKnight) enemy;
                if (fk.triggerHeavyShake) {
                    triggerShake(12f, 0.4f);
                    fk.triggerHeavyShake = false;
                }
                if (fk.triggerNormalShake) {
                    triggerShake(8f, 0.2f);
                    fk.triggerNormalShake = false;
                }
                if (fk.triggerAttackShake) {
                    triggerShake(4f, 0.3f);
                    fk.triggerAttackShake = false;
                }
            }
        }
    }

    // --- Entity & Game State Management ---
    private void registerDeaths() {
        for (Enemy e : enemies) {
            if (e.hasUnregisteredDeath()) {
                e.registerDeath();
                numberOfEnemiesKilled++;
                AchievementManager.getInstance().notifyEnemyDeath(e);
            }
        }
        if (player.hasUnregisteredDeath()) {
            player.registerDeath();
            numberOfDeaths++;
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

    private void updateProjectiles(float delta) {
        java.util.Iterator<PlayerProjectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            PlayerProjectile proj = iterator.next();
            proj.update(delta, solidBlocks, enemies);
            if (proj.isFinished)
                iterator.remove();
        }
    }

    // --- Boss Logic ---
    private void configureFalseKnightArena(FalseKnight falseKnight) {
        falseKnight.setArenaWalls(bossDoor, bossRoomRightWall);
    }

    private void updateBossFight(float delta) {
        // 1. Check Activation Trigger
        if (activationZone != null && !bossFightActivated && !bossFightCompleted) {
            if (player.getBounds().overlaps(activationZone)) {
                bossFightActivated = true;

                if (falseKnightSpawnPoint != null) {
                    Enemy boss = EnemyFactory.newEnemy(falseKnightSpawnPoint, EnemyType.FALSE_KNIGHT);
                    if (boss instanceof FalseKnight) {
                        configureFalseKnightArena((FalseKnight) boss);
                    }
                    enemies.add(boss);
                }

                solidBlocks.add(bossDoor);
            }
        }

        // 2. Check Boss Defeat
        if (bossFightActivated && !bossFightCompleted) {
            boolean bossIsDead = true;
            for (Enemy enemy : enemies) {
                if (enemy instanceof FalseKnight) {
                    if (!enemy.isDead) {
                        bossIsDead = false;
                        break;
                    }
                }
            }

            if (bossIsDead) {
                bossFightActivated = false;
                bossFightCompleted = true;

                AchievementManager.getInstance().onBossDefeated();
                AudioController.getInstance().transitionBgm(GameAssetManager.gameEndMusic, 5f);
                solidBlocks.remove(bossDoor);
            }
        }

        // 3. Check Game End Entry
        if (bossFightCompleted && !bossJustDefeated && gameEndZone != null) {
            if (player.getBounds().overlaps(gameEndZone)) {
                bossJustDefeated = true;
                float totalTimeInSeconds = totalPassedTime + passedTimeBuffer;
                AchievementManager.getInstance().onGameCompleted(totalTimeInSeconds);
            }
        }

        // 4. Resolve Door Animations
        if (bossFightActivated) {
            gateDropProgress += delta * 4f;
            if (gateDropProgress > 1f)
                gateDropProgress = 1f;
        } else if (bossFightCompleted) {
            gateDropProgress -= delta * 2f;
            if (gateDropProgress < 0f)
                gateDropProgress = 0f;
        }
    }

    // --- Collision & Combat Validation ---
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
                if (!player.isInvinvible()) {
                    player.takeDamage(Constants.HAZARD_DAMAGE, hazard.getBounds().x + (hazard.getBounds().width / 2f),
                            false);
                    player.position.set(player.lastSafePosition);
                    player.velocity.setZero();
                }
            }
            break;
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

    private void checkEnemyCollisions() {
        if (player.getVitals().isDead() || player.isInvinvible())
            return;

        Rectangle playerBounds = player.getBounds();

        for (Enemy enemy : enemies) {
            if (enemy.isDead)
                continue;

            if (!(enemy instanceof FalseKnight)
                    && player.position.dst(enemy.position) > Constants.ENEMY_ACTIVE_RADIUS) {
                continue;
            }

            if (enemy instanceof FalseKnight) {
                FalseKnight fk = (FalseKnight) enemy;

                Rectangle attackHitbox = fk.getActiveAttackHitbox();
                if (attackHitbox != null && playerBounds.overlaps(attackHitbox)) {
                    player.takeDamage(FalseKnight.FALSE_KNIGHT_ATTACK_DAMAGE, fk.position.x);
                    return;
                }

                for (FalseKnight.Shockwave wave : fk.shockwaves) {
                    if (playerBounds.overlaps(wave.bounds)) {
                        player.takeDamage(wave.damage, wave.bounds.x);
                        return;
                    }
                }
            }

            if (playerBounds.overlaps(enemy.getBounds())) {
                if (player.shouldDamageWithDash(enemy)) {
                    player.hitEnemyWithDash(enemy);
                } else if (player.shouldTakeDamageWithCollision()) {
                    player.takeDamage(enemy.getCollisionDamage(), enemy.position.x);
                }
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
                    enemy.takeDamage(player.getNailDamage(), player.position.x, true, player.getKnockbackMultiplier());
                    enemiesHitThisAttack.add(enemy);
                    player.getVitals().addSouls(player.getSoulHitBonus());
                    if (isDownSlashing)
                        player.pogo();
                }
            }
        }

        if (zote != null && attackBounds.overlaps(zote.getBounds())) {
            zote.triggerAnger();
            if (isDownSlashing) {
                player.pogo();
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
                    enemy.takeDamage(player.getSpellDamage(Constants.SOUL_SCREAM_TICK_DAMAGE), player.position.x, true,
                            0.5f);
                }
            }
            player.triggerScreamDamage = false;
        }
    }

    private void checkSpiritCastAttacks() {
        if (player.combatState == CombatState.CAST && player.triggerSpiritCast) {
            Vector2 spawnPos = new Vector2(player.position.x,
                    player.position.y + Constants.PLAYER_HITBOX_HEIGHT / 2 - Constants.PROJECTILE_SIZE / 2);
            projectiles.add(new PlayerProjectile(spawnPos, player.getDirection(),
                    player.getSpellDamage(Constants.PROJECTILE_DAMAGE)));
            player.triggerSpiritCast = false;
        }
    }

    // --- Utilities & Accessors ---
    public void triggerShake(float intensity, float duration) {
        this.cameraShakeIntensity = Math.max(this.cameraShakeIntensity, intensity);
        this.cameraShakeTimer = Math.max(this.cameraShakeTimer, duration);
    }

    public void applyCheat(GameCheat cheat) {
        if (cheat == GameCheat.KILL_ENEMIES) {
            for (Enemy enemy : enemies) {
                if (player.position.dst(enemy.position) <= Constants.ENEMY_ACTIVE_RADIUS) {
                    enemy.kill();
                }
            }
        } else if (cheat == GameCheat.TP_TO_BOSS) {
            player.position.set(playerTPPoint);
        } else {
            player.applyCheat(cheat);
        }
    }

    public boolean isBossFightActivated() {
        return bossFightActivated && !bossFightCompleted;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getNumberOfEnemiesKilled() {
        return numberOfEnemiesKilled;
    }

    public int getNumberOfDeaths() {
        return numberOfDeaths;
    }

    public long getTotalPassedTime() {
        return totalPassedTime;
    }
}
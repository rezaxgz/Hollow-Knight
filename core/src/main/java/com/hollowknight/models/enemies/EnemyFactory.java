package com.hollowknight.models.enemies;

import com.badlogic.gdx.math.Vector2;

public class EnemyFactory {
    public static Enemy newEnemy(Vector2 point, EnemyType enemyType) {
        Enemy enemy = null;
        switch (enemyType) {
            case CRAWLID:
                enemy = GroundEnemy.newEnemy(GroundEnemyType.CRAWLID, point);
                break;
            case HUSK_HORNHEAD:
                enemy = HuskHornHead.newEnemy(point);
                break;
            case MOSSCREEP:
                enemy = GroundEnemy.newEnemy(GroundEnemyType.MOSSCREEP, point);
                break;
            case MOSSFLY:
                enemy = Mossfly.newEnemy(point);
                break;
            case CRYSTAL_GAURDIAN:
                enemy = CrystalGuardian.newEnemy(point);
                break;
            case FALSE_KNIGHT:
                enemy = FalseKnight.newEnemy(point);
                break;
        }
        enemy.type = enemyType;
        return enemy;
    }
}

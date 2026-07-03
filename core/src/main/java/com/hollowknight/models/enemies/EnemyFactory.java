package com.hollowknight.models.enemies;

import com.badlogic.gdx.math.Vector2;

public class EnemyFactory {
    public static Enemy newEnemy(Vector2 point, EnemyType enemyType) {
        switch (enemyType) {
            case CRAWLID:
                return GroundEnemy.newEnemy(GroundEnemyType.CRAWLID, point);
            case HUSK_HORNHEAD:
                return HuskHornHead.newEnemy(point);
            case MOSSCREEP:
                return GroundEnemy.newEnemy(GroundEnemyType.MOSSCREEP, point);
            case MOSSFLY:
                return Mossfly.newEnemy(point);
            case CRYSTAL_GAURDIAN:
                return CrystalGuardian.newEnemy(point);
            default:
                return null;
        }
    }
}

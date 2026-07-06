package com.hollowknight.models.enemies;

public enum EnemyType {
    // Ground Enemies
    CRAWLID,
    MOSSCREEP,

    // Flying Enemies
    MOSSFLY,

    // Special Enemies
    HUSK_HORNHEAD,
    CRYSTAL_GAURDIAN,

    // Boss
    FALSE_KNIGHT;

    public static EnemyType fromInt(int n) {
        return values()[Math.floorMod(n, values().length)];
    }
}

package com.hollowknight.models.achievements;

import com.hollowknight.models.enemies.Enemy;
import com.hollowknight.models.enemies.EnemyType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AchievementManager {
    private static AchievementManager instance;
    private final HashMap<String, Achievement> achievements = new HashMap<>();
    private AchievementObserver observer;

    // Set to track unique enemy classes killed
    private final HashSet<EnemyType> killedEnemyTypes = new HashSet<>();

    public static AchievementManager getInstance() {
        if (instance == null)
            instance = new AchievementManager();
        return instance;
    }

    private AchievementManager() {
        achievements.put("COMPLETION",
                new Achievement("COMPLETION", "PURE COMPLETION", "Finish the game by defeating the False Knight.",
                        "achievements/completion.png"));
        achievements.put("SPEEDRUN",
                new Achievement("SPEEDRUN", "SPEEDRUNNER", "Complete the game in under 30 minutes.",
                        "achievements/speedrun.png"));
        achievements.put("TRUE_HUNTER",
                new Achievement("TRUE_HUNTER", "TRUE HUNTER", "Defeat every unique type of enemy.",
                        "achievements/hunter.png"));
        achievements.put("FALSE_KNIGHT",
                new Achievement("FALSE_KNIGHT", "DEFEAT FALSE KNIGHT", "Vanquish the False Knight boss.",
                        "achievements/false knight.png"));
    }

    public void setObserver(AchievementObserver observer) {
        this.observer = observer;
    }

    public Collection<Achievement> getAchievements() {
        return achievements.values();
    }

    public void unlock(String id) {
        Achievement ach = achievements.get(id);
        if (ach != null && !ach.isUnlocked()) {
            ach.setUnlocked(true);
            System.out.println("[ACHIEVEMENT UNLOCKED]: " + ach.title);
            if (observer != null) {
                observer.onAchievementUnlocked(ach);
            }
        }
    }

    public List<String> getUnlockedIds() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Achievement> entry : achievements.entrySet()) {
            if (entry.getValue().isUnlocked()) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public void loadUnlockedAchievements(List<String> unlockedIds) {
        for (String id : unlockedIds) {
            Achievement ach = achievements.get(id);
            if (ach != null) {
                ach.setUnlocked(true);
            }
        }
    }

    // Called when the False Knight is killed
    public void onBossDefeated(float totalPlayTimeSeconds) {
        unlock("FALSE_KNIGHT");
        unlock("COMPLETION");

        // 30 minutes = 1800 seconds
        if (totalPlayTimeSeconds <= 1800f) {
            unlock("SPEEDRUN");
        }
    }

    // Called when any enemy is killed
    public void notifyEnemyDeath(Enemy enemy) {
        // Automatically grabs the class name (e.g., "Crawlid", "CrystalGuardian")
        EnemyType enemyType = enemy.type;
        boolean isNew = killedEnemyTypes.add(enemyType);
        if (isNew) {
            System.out.println("registered " + enemyType.toString());
        }

        // -1 for false knight
        if (killedEnemyTypes.size() >= EnemyType.values().length - 1) {
            unlock("TRUE_HUNTER");
        }
    }
}
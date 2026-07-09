package com.hollowknight.models.achievements;

public interface AchievementObserver {
    void onAchievementUnlocked(Achievement achievement);
}
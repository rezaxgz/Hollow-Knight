package com.hollowknight.models.achievements;

public class Achievement {
    public final String id;
    public final String title;
    public final String description;
    public final String iconPath;
    private boolean unlocked;

    public Achievement(String id, String title, String description, String iconPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconPath = iconPath;
        this.unlocked = false;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
package com.hollowknight.models.achievements;

public class Achievement {
    public final String id;
    public final String title;
    public final String description;
    // Removed iconPath for simplicity, but you can add it back if you have the
    // textures
    private boolean unlocked;

    public Achievement(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.unlocked = false;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
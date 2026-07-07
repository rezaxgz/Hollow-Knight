package com.hollowknight.models.player;

public enum CharmType {
    SOUL_CATCHER("Soul Catcher", "sprites/Inventory & UI/Charms/Soul Catcher.png",
            "Increases the amount of Soul gained from successful hits with the Nail."),
    DASH_MASTER("Dash Master", "sprites/Inventory & UI/Charms/Dashmaster.png",
            "Allows the player to dash at shorter intervals."),
    UNBREAKABLE_STRENGHT("Unbreakable Strength", "sprites/Inventory & UI/Charms/Unbreakable Strength.png",
            "Strengthens the Knight, increasing damage dealt to enemies with the Nail."),
    QUICK_SLASH("Quick Slash", "sprites/Inventory & UI/Charms/Quick Slash.png",
            "Greatly increases Nail attack speed and reduces cooldown after each hit."),
    QUICK_FOCUS("Quick Focus", "sprites/Inventory & UI/Charms/Quick Focus.png",
            "Increases focus/heal speed and reduces the time required to heal."),
    HEAVY_BLOW("Heavy Blow", "sprites/Inventory & UI/Charms/Heavy Blow.png",
            "Increases knockback force, causing enemies to be knocked back further when damaged."),
    SHARP_SHADOW("Sharp Shadow", "sprites/Inventory & UI/Charms/Sharp Shadow.png",
            "Dash through enemies to deal damage; also increases dash length by 20%."),
    VOID_HEART("Void Heart", "sprites/Inventory & UI/Charms/Void Heart.png",
            "Upgrades spells, increases damage by 50%, and enables dark animations for abilities.");

    public final String name;
    public final String path;
    public final String description;

    CharmType(String name, String path, String description) {
        this.name = name;
        this.path = path;
        this.description = description;
    }
}
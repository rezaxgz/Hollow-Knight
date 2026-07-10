package com.hollowknight.views.theme;

public enum MenuThemeType {
    CLASSIC_HOLLOW("classic_hollow", "Classic Hollow"),
    VOIDHEART("voidheart", "Voidheart"),
    ROYAL_GOLD("royal_gold", "Royal Gold");

    private final String id;
    private final String displayName;

    MenuThemeType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MenuThemeType next() {
        MenuThemeType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static MenuThemeType fromId(String id) {
        if (id == null) {
            return VOIDHEART;
        }
        for (MenuThemeType theme : values()) {
            if (theme.id.equals(id)) {
                return theme;
            }
        }
        return VOIDHEART;
    }

    public static String[] displayNames() {
        MenuThemeType[] values = values();
        String[] names = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            names[index] = values[index].getDisplayName();
        }
        return names;
    }

    public static MenuThemeType fromDisplayName(String displayName) {
        if (displayName == null) {
            return VOIDHEART;
        }
        for (MenuThemeType theme : values()) {
            if (theme.displayName.equals(displayName)) {
                return theme;
            }
        }
        return VOIDHEART;
    }
}
package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets Category
 */
public enum Category {
    ADMIN_TOOLS("admin_tools"),
    CHAT("chat"),
    DEV_TOOLS("dev_tools"),
    ECONOMY("economy"),
    GAMEPLAY("gameplay"),
    GAMES("games"),
    PROTECTION("protection"),
    ROLE_PLAYING("role_playing"),
    WORLD_MANAGEMENT("world_management"),
    MISC("misc");

    private final String value;

    Category(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Category fromValue(String text) {
        for (Category b : Category.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}

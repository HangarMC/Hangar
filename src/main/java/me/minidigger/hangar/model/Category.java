package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Category {
    ADMIN_TOOLS(0, "Admin Tools", "fa-server", "admin_tools"),
    CHAT(1, "Chat", "fa-comment", "chat"),
    DEV_TOOLS(2, "Developer Tools", "fa-wrench", "dev_tools"),
    ECONOMY(3, "Economy", "fa-money-bill-alt", "economy"),
    GAMEPLAY(4, "Gameplay", "fa-puzzle-piece", "gameplay"),
    GAMES(5, "Games", "fa-gamepad", "games"),
    PROTECTION(6, "Protection", "fa-lock", "protection"),
    ROLE_PLAYING(7, "Role Playing", "fa-magic", "role_playing"),
    WORLD_MANAGEMENT(8, "World Management", "fa-globe", "world_management"),
    MISC(9, "Miscellaneous", "fa-asterisk", "misc"),
    UNDEFINED(10, "Undefined", "", "undefined");

    private final int value;
    private final String title;
    private final String icon;
    private final boolean isVisible;
    private final String apiName;

    Category(int value, String title, String icon, String apiName) {
        this(value, title, icon, apiName, true);
    }

    Category(int value, String title, String icon, String apiName, boolean isVisible) {
        this.value = value;
        this.title = title;
        this.icon = icon;
        this.apiName = apiName;
        this.isVisible = isVisible;
    }

    public int getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public String getApiName() {
        return apiName;
    }

    @Override
    @JsonValue
    public String toString() {
        return apiName;
    }

    @JsonCreator
    public static Category fromValue(String text) {
        for (Category b : Category.values()) {
            if (b.apiName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public static Category fromTitle(String text) {
        for (Category b : Category.values()) {
            if (b.title.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public static Set<Category> visible() {
        return Arrays.stream(Category.values()).filter(Category::isVisible).collect(Collectors.toSet());
    }
}

package io.papermc.hangar.model.common.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Category {
    ADMIN_TOOLS(0, "Admin Tools", "mdi-server", "admin_tools"),
    CHAT(1, "Chat", "mdi-chat", "chat"),
    DEV_TOOLS(2, "Developer Tools", "mdi-wrench", "dev_tools"),
    ECONOMY(3, "Economy", "mdi-cash-multiple", "economy"),
    GAMEPLAY(4, "Gameplay", "mdi-puzzle", "gameplay"),
    GAMES(5, "Games", "mdi-controller-classic", "games"),
    PROTECTION(6, "Protection", "mdi-lock", "protection"),
    ROLE_PLAYING(7, "Role Playing", "mdi-auto-fix", "role_playing"),
    WORLD_MANAGEMENT(8, "World Management", "mdi-earth", "world_management"),
    MISC(9, "Miscellaneous", "mdi-asterisk", "misc"),
    UNDEFINED(10, "Undefined", "", "undefined", false);

    private final int value;
    private final String title;
    private final String icon;
    private final boolean isVisible;
    private final String apiName;

    public static final Category[] VALUES = values();

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

    @JsonIgnore
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
        return fromTitle(text);
    }

    public static Category fromTitle(String text) {
        for (Category b : Category.values()) {
            if (b.title.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public static List<Category> fromString(String str) {
        List<Category> categories = new ArrayList<>();
        Arrays.stream(str.split(",")).forEach(s -> {
            try {
                int id = Integer.parseInt(s);
                categories.add(Category.VALUES[id]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) { }
        });
        return categories;
    }

    public static Set<Category> visible() {
        return Arrays.stream(Category.values()).filter(Category::isVisible).collect(Collectors.toSet());
    }
}

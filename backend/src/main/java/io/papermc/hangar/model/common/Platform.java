package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@EnumByOrdinal
public enum Platform {

    // NOTE: The order here should always be the order they are displayed whenever there is a list somewhere on the frontend
    PAPER("Paper", Category.SERVER, "https://papermc.io/downloads", true),
    WATERFALL("Waterfall", Category.PROXY, "https://papermc.io/downloads#Waterfall", true),
    VELOCITY("Velocity", Category.PROXY, "https://www.velocitypowered.com/downloads", true);

    private static final Platform[] VALUES = values();

    private final String name;
    private final Category category;
    private final boolean isVisible;
    private final String url;

    Platform(String name, Category category, String url, boolean isVisible) {
        this.name = name;
        this.category = category;
        this.url = url;
        this.isVisible = isVisible;
    }

    public String getName() {
        return name;
    }

    @JsonValue
    public String getEnumName() {
        return name();
    }

    public Category getCategory() {
        return category;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public String getUrl() {
        return url;
    }

    public static Platform[] getValues() {
        return VALUES;
    }

    public enum Category {
        SERVER("Server Plugins", "Server"),
        PROXY("Proxy Plugins", "Proxy");

        private final String name;
        private final String tagName;
        private Set<Platform> platforms;

        Category(String name, String tagName) {
            this.name = name;
            this.tagName = tagName;
        }

        public String getName() {
            return name;
        }

        @JsonValue
        public String getTagName() {
            return tagName;
        }

        public Set<Platform> getPlatforms() {
            if (platforms != null) {
                platforms = Arrays.stream(Platform.VALUES).filter(p -> p.category == this).collect(Collectors.toSet());
            }
            return platforms;
        }
    }
}

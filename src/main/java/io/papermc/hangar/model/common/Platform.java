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

    PAPER("Paper", Category.SERVER, TagColor.PAPER, "https://papermc.io/downloads"),
    WATERFALL("Waterfall", Category.PROXY, TagColor.WATERFALL, "https://papermc.io/downloads#Waterfall"),
    VELOCITY("Velocity", Category.PROXY, TagColor.VELOCITY, "https://www.velocitypowered.com/downloads");

    private static final Platform[] VALUES = values();

    private final String name;
    private final Category category;
    private final TagColor tagColor;
    private final String url;

    Platform(String name, Category category, TagColor tagColor, String url) {
        this.name = name;
        this.category = category;
        this.tagColor = tagColor;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getEnumName() { return name(); }

    public Category getCategory() {
        return category;
    }

    public TagColor getTagColor() {
        return tagColor;
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

        public static Category[] VALUES = values();

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

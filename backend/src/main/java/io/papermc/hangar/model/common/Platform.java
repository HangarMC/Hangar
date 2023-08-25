package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.jdbi.v3.core.enums.EnumByOrdinal;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@EnumByOrdinal
@Schema(description = "Server platform", example = "PAPER")
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

    Platform(final String name, final Category category, final String url, final boolean isVisible) {
        this.name = name;
        this.category = category;
        this.url = url;
        this.isVisible = isVisible;
    }

    public String getName() {
        return this.name;
    }

    @JsonValue
    public String getEnumName() {
        return this.name();
    }

    public Category getCategory() {
        return this.category;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public String getUrl() {
        return this.url;
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

        Category(final String name, final String tagName) {
            this.name = name;
            this.tagName = tagName;
        }

        public String getName() {
            return this.name;
        }

        @JsonValue
        public String getTagName() {
            return this.tagName;
        }

        public Set<Platform> getPlatforms() {
            if (this.platforms != null) {
                this.platforms = Arrays.stream(VALUES).filter(p -> p.category == this).collect(Collectors.toSet());
            }
            return this.platforms;
        }
    }
}

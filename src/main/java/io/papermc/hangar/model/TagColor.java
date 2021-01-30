package io.papermc.hangar.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import org.jetbrains.annotations.Nullable;

@JsonFormat(shape = Shape.OBJECT)
public enum TagColor {

    PAPER("#F7CF0D", "#333333"),
    WATERFALL("#F7CF0D", "#333333"),
    VELOCITY("#039BE5","#333333"),

    UNSTABLE("#F54242", "#333333");

    public static final TagColor[] VALUES = TagColor.values();

    private final String background;
    private final String foreground;

    TagColor(String background, String foreground) {
        this.background = background;
        this.foreground = foreground;
    }

    public String getBackground() {
        return background;
    }

    public String getForeground() {
        return foreground;
    }

    public io.papermc.hangar.model.api.color.TagColor toTagColor() {
        return new io.papermc.hangar.model.api.color.TagColor(foreground, background);
    }

    @Nullable
    public static TagColor getByName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        for (TagColor color : VALUES) {
            if (color.name().equalsIgnoreCase(name)) {
                return color;
            }
        }
        return null;
    }
}

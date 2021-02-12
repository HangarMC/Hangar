package io.papermc.hangar.model.api.color;

import java.util.Objects;

public class TagColor {

    private final String foreground;
    private final String background;

    public TagColor(String foreground, String background) {
        this.foreground = foreground;
        this.background = background;
    }

    public String getForeground() {
        return foreground;
    }

    public String getBackground() {
        return background;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagColor tagColor = (TagColor) o;
        return Objects.equals(foreground, tagColor.foreground) && Objects.equals(background, tagColor.background);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background);
    }

    @Override
    public String toString() {
        return "TagColor{" +
                "foreground='" + foreground + '\'' +
                ", background='" + background + '\'' +
                '}';
    }
}

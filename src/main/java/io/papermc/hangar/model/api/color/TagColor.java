package io.papermc.hangar.model.api.color;

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
    public String toString() {
        return "TagColor{" +
                "foreground='" + foreground + '\'' +
                ", background='" + background + '\'' +
                '}';
    }
}

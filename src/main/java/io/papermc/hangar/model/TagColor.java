package io.papermc.hangar.model;

public enum TagColor { // remember, once we push to production, the order of these enums cannot change

    PAPER("#F7CF0D", "#333333"),
    WATERFALL("#F7CF0D", "#333333"),
    VELOCITY("#039BE5","#333333"),

    UNSTABLE("#FFDAB9", "#333333");

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

    public static final TagColor[] VALUES = TagColor.values();
}

package me.minidigger.hangar.model;

public enum TagColor { // remember, once we push to production, the order of these enums cannot change

    PAPER("#F7Cf0D", "#333333"),
    WATERFALL("#dfa86a", "#FFFFFF"),
    VELOCITY("#910020","#FFFFFF"),

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
}

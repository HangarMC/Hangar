package io.papermc.hangar.model.api.project.settings;

public enum LinkSectionType {

    TOP(5, false),
    SIDEBAR(10, true);

    private final int maxLinks; // TODO config
    private final boolean hasTitle;

    LinkSectionType(final int maxLinks, final boolean hasTitle) {
        this.maxLinks = maxLinks;
        this.hasTitle = hasTitle;
    }

    public int maxLinks() {
        return this.maxLinks;
    }

    public boolean hasTitle() {
        return this.hasTitle;
    }
}

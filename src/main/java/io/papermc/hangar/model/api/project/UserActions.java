package io.papermc.hangar.model.api.project;

public class UserActions {

    private final boolean starred;
    private final boolean watching;

    public UserActions(boolean starred, boolean watching) {
        this.starred = starred;
        this.watching = watching;
    }

    public boolean isStarred() {
        return starred;
    }

    public boolean isWatching() {
        return watching;
    }
}

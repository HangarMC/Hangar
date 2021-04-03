package io.papermc.hangar.model.api.project;

public class UserActions {

    private final boolean starred;
    private final boolean watching;
    private final boolean flagged;

    public UserActions(boolean starred, boolean watching, boolean flagged) {
        this.starred = starred;
        this.watching = watching;
        this.flagged = flagged;
    }

    public boolean isStarred() {
        return starred;
    }

    public boolean isWatching() {
        return watching;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @Override
    public String toString() {
        return "UserActions{" +
                "starred=" + starred +
                ", watching=" + watching +
                ", flagged=" + flagged +
                '}';
    }
}

package io.papermc.hangar.model.api.project;

public class UserActions {

    private final boolean starred;
    private final boolean watching;
    private final boolean flagged;

    public UserActions(final boolean starred, final boolean watching, final boolean flagged) {
        this.starred = starred;
        this.watching = watching;
        this.flagged = flagged;
    }

    public boolean isStarred() {
        return this.starred;
    }

    public boolean isWatching() {
        return this.watching;
    }

    public boolean isFlagged() {
        return this.flagged;
    }

    @Override
    public String toString() {
        return "UserActions{" +
                "starred=" + this.starred +
                ", watching=" + this.watching +
                ", flagged=" + this.flagged +
                '}';
    }
}

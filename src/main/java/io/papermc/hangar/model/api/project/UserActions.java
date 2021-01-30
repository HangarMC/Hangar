package io.papermc.hangar.model.api.project;

import org.jdbi.v3.core.mapper.PropagateNull;

public class UserActions {

    private final boolean starred;
    private final boolean watching;

    public UserActions(@PropagateNull boolean starred, boolean watching) {
        this.starred = starred;
        this.watching = watching;
    }

    public boolean isStarred() {
        return starred;
    }

    public boolean isWatching() {
        return watching;
    }

    @Override
    public String toString() {
        return "UserActions{" +
                "starred=" + starred +
                ", watching=" + watching +
                '}';
    }
}

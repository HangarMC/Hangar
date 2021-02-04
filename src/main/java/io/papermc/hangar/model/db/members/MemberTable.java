package io.papermc.hangar.model.db.members;

public abstract class MemberTable {

    private final long userId;

    protected MemberTable(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
}

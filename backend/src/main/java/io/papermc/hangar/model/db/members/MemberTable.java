package io.papermc.hangar.model.db.members;

public abstract class MemberTable {

    private final long userId;

    protected MemberTable(final long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return this.userId;
    }
}

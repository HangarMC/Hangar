package io.papermc.hangar.model.db.log;

import io.papermc.hangar.db.customtypes.LoggedAction;

import java.net.InetAddress;

public class LoggedActionsUserTable extends LoggedActionTable {

    private final long subjectId;

    public LoggedActionsUserTable(long userId, InetAddress address, LoggedAction action, String newState, String oldState, long subjectId) {
        super(userId, address, action, newState, oldState);
        this.subjectId = subjectId;
    }

    public long getSubjectId() {
        return subjectId;
    }

    @Override
    public String toString() {
        return "LoggedActionsUserTable{" +
                "subjectId=" + subjectId +
                "} " + super.toString();
    }
}

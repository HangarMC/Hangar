package io.papermc.hangar.model.db.log;

import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;

import java.net.InetAddress;

public class LoggedActionsUserTable extends LoggedActionTable {

    private final long subjectId;

    public LoggedActionsUserTable(long userId, InetAddress address, LoggedAction<UserContext> action) {
        super(userId, address, action);
        this.subjectId = action.getContext().getUserId();
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

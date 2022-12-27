package io.papermc.hangar.model.db.log;

import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import java.net.InetAddress;

public class LoggedActionsUserTable extends LoggedActionTable {

    private final long subjectId;

    public LoggedActionsUserTable(final long userId, final InetAddress address, final LoggedAction<UserContext> action) {
        super(userId, address, action);
        this.subjectId = action.getContext().getUserId();
    }

    public long getSubjectId() {
        return this.subjectId;
    }

    @Override
    public String toString() {
        return "LoggedActionsUserTable{" +
            "subjectId=" + this.subjectId +
            "} " + super.toString();
    }
}

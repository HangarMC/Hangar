package io.papermc.hangar.model.internal.logs;

import io.papermc.hangar.model.db.log.LoggedActionTable;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import java.util.Objects;

public class LoggedAction<LC extends LogContext<? extends LoggedActionTable, LC>> {

    private final LogAction<LC> type;
    private final LC context;
    private final String newState;
    private final String oldState;

    LoggedAction(final LogAction<LC> type, final LC context, final String newState, final String oldState) {
        this.type = type;
        this.context = context;
        this.newState = Objects.requireNonNull(newState);
        this.oldState = Objects.requireNonNull(oldState);
    }

    public LogAction<LC> getType() {
        return this.type;
    }

    public LC getContext() {
        return this.context;
    }

    public String getNewState() {
        return this.newState;
    }

    public String getOldState() {
        return this.oldState;
    }
}

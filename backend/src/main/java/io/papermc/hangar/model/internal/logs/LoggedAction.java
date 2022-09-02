package io.papermc.hangar.model.internal.logs;

import io.papermc.hangar.model.db.log.LoggedActionTable;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;

import java.util.Objects;

public class LoggedAction<LC extends LogContext<? extends LoggedActionTable, LC>> {

    private final LogAction<LC> type;
    private final LC context;
    private final String newState;
    private final String oldState;

    LoggedAction(LogAction<LC> type, LC context, String newState, String oldState) {
        this.type = type;
        this.context = context;
        this.newState = Objects.requireNonNull(newState);
        this.oldState = Objects.requireNonNull(oldState);
    }

    public LogAction<LC> getType() {
        return type;
    }

    public LC getContext() {
        return context;
    }

    public String getNewState() {
        return newState;
    }

    public String getOldState() {
        return oldState;
    }
}

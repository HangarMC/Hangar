package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionTable;
import io.papermc.hangar.model.internal.logs.LoggedAction;

import java.net.InetAddress;

public abstract class LogContext<LT extends LoggedActionTable, LC extends LogContext<LT, LC>> {

    protected final Context contextType;
    private final LogTableConstructor<LT, LC> tableConstructor;

    protected LogContext(Context contextType, LogTableConstructor<LT, LC> tableConstructor) {
        this.contextType = contextType;
        this.tableConstructor = tableConstructor;
    }

    public final Context getContextType() {
        return contextType;
    }

    public final LT createTable(long userId, InetAddress address, LoggedAction<LC> action) {
        return tableConstructor.create(userId, address, action);
    }
}

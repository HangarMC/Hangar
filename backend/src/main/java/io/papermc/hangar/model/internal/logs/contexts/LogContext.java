package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionTable;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import java.net.InetAddress;

public abstract class LogContext<LT extends LoggedActionTable, LC extends LogContext<LT, LC>> {

    protected final Context contextType;
    private final LogTableConstructor<LT, LC> tableConstructor;

    protected LogContext(final Context contextType, final LogTableConstructor<LT, LC> tableConstructor) {
        this.contextType = contextType;
        this.tableConstructor = tableConstructor;
    }

    public final Context getContextType() {
        return this.contextType;
    }

    public final LT createTable(final long userId, final InetAddress address, final LoggedAction<LC> action) {
        return this.tableConstructor.create(userId, address, action);
    }
}

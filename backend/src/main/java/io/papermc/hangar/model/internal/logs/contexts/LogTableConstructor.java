package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionTable;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import java.net.InetAddress;

@FunctionalInterface
public interface LogTableConstructor<LT extends LoggedActionTable, LC extends LogContext<LT, LC>> {

    LT create(long userId, InetAddress address, LoggedAction<LC> action);
}

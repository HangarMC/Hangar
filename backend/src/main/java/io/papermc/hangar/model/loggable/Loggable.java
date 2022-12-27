package io.papermc.hangar.model.loggable;

import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.service.internal.UserActionLogService;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public interface Loggable<LC extends LogContext<?, LC>> {

    default void logAction(final UserActionLogService actionLogger, final LogAction<LC> logAction, final @NotNull String newState, final @NotNull String oldState) {
        this.getLogInserter(actionLogger).accept(logAction.create(this.createLogContext(), newState, oldState));
    }

    Consumer<LoggedAction<LC>> getLogInserter(UserActionLogService actionLogger);

    LC createLogContext();
}

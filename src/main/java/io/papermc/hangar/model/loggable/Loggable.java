package io.papermc.hangar.model.loggable;

import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.service.internal.UserActionLogService;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface Loggable<LC extends LogContext<?, LC>> {

    default void logAction(UserActionLogService actionLogger, LogAction<LC> logAction, @NotNull String newState, @NotNull String oldState) {
        this.getLogInserter(actionLogger).accept(logAction.create(this.createLogContext(), newState, oldState));
    }

    Consumer<LoggedAction<LC>> getLogInserter(UserActionLogService actionLogger);

    LC createLogContext();
}

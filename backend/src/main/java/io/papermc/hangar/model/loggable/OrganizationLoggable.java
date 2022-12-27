package io.papermc.hangar.model.loggable;

import io.papermc.hangar.model.identified.OrganizationIdentified;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.service.internal.UserActionLogService;
import java.util.function.Consumer;

public interface OrganizationLoggable extends Loggable<OrganizationContext>, OrganizationIdentified {

    @Override
    default Consumer<LoggedAction<OrganizationContext>> getLogInserter(final UserActionLogService actionLogger) {
        return actionLogger::organization;
    }

    @Override
    default OrganizationContext createLogContext() {
        return OrganizationContext.of(this.getOrganizationId());
    }
}

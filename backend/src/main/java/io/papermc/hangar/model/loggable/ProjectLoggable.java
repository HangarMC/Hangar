package io.papermc.hangar.model.loggable;

import io.papermc.hangar.model.identified.ProjectIdentified;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.service.internal.UserActionLogService;
import java.util.function.Consumer;

public interface ProjectLoggable extends Loggable<ProjectContext>, ProjectIdentified {

    @Override
    default Consumer<LoggedAction<ProjectContext>> getLogInserter(final UserActionLogService actionLogger) {
        return actionLogger::project;
    }

    @Override
    default ProjectContext createLogContext() {
        return ProjectContext.of(this.getProjectId());
    }
}

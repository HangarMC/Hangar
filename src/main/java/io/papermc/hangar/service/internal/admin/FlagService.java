package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectFlagsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectFlagsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class FlagService extends HangarComponent {

    private final ProjectFlagsDAO projectFlagsDAO;
    private final HangarProjectFlagsDAO hangarProjectFlagsDAO;

    @Autowired
    public FlagService(ProjectFlagsDAO projectFlagsDAO, HangarProjectFlagsDAO hangarProjectFlagsDAO) {
        this.projectFlagsDAO = projectFlagsDAO;
        this.hangarProjectFlagsDAO = hangarProjectFlagsDAO;
    }

    public void createFlag(long projectId, FlagReason reason, String comment) {
        if (hasUnresolvedFlag(projectId, getHangarPrincipal().getId())) {
            throw new HangarApiException("project.flag.error.alreadyOpen");
        }
        projectFlagsDAO.insert(new ProjectFlagTable( projectId, getHangarPrincipal().getId(), reason, comment));
        actionLogger.project(LogAction.PROJECT_FLAGGED.create(ProjectContext.of(projectId), "Flagged by " + getHangarPrincipal().getName(), ""));
    }

    public boolean hasUnresolvedFlag(long projectId, long userId) {
        return projectFlagsDAO.getUnresolvedFlag(projectId, userId) != null;
    }

    public void markAsResolved(long flagId, boolean resolved) {
        HangarProjectFlag hangarProjectFlag = hangarProjectFlagsDAO.getById(flagId);
        if (hangarProjectFlag == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (hangarProjectFlag.isResolved()) {
            throw new HangarApiException("project.flag.error.alreadyResolved");
        }
        Long resolvedBy = resolved ? getHangarPrincipal().getId() : null;
        OffsetDateTime resolvedAt = resolved ? OffsetDateTime.now() : null;
        projectFlagsDAO.markAsResolved(flagId, resolved, resolvedBy, resolvedAt);
        hangarProjectFlag.logAction(actionLogger, LogAction.PROJECT_FLAG_RESOLVED, "Flag resolved by " + getHangarPrincipal().getName(), "Flag reported by " + hangarProjectFlag.getReportedByName());
    }

    public List<HangarProjectFlag> getFlags(long projectId) {
        return hangarProjectFlagsDAO.getFlags(projectId);
    }

    public List<HangarProjectFlag> getFlags() {
        return hangarProjectFlagsDAO.getFlags();
    }
}

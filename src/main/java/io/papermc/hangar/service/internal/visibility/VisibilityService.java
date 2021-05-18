package io.papermc.hangar.service.internal.visibility;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.ModelVisible;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.db.visibility.VisibilityChangeTable;
import io.papermc.hangar.model.identified.ProjectIdentified;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.loggable.Loggable;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.JobService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map.Entry;

abstract class VisibilityService<LC extends LogContext<?, LC>, M extends Table & ModelVisible & ProjectIdentified & Loggable<LC>, VT extends VisibilityChangeTable> extends HangarComponent {

    @Autowired
    private PermissionService permissionService;

    private final VisibilityChangeTableConstructor<VT> changeTableConstructor;
    private final LogAction<LC> visibilityChangeLogAction;

    protected VisibilityService(VisibilityChangeTableConstructor<VT> changeTableConstructor, LogAction<LC> visibilityChangeLogAction) {
        this.changeTableConstructor = changeTableConstructor;
        this.visibilityChangeLogAction = visibilityChangeLogAction;
    }

    public M changeVisibility(long modelId, Visibility newVisibility, String comment) {
        return changeVisibility(getModel(modelId), newVisibility, comment);
    }

    public M changeVisibility(@Nullable M model, Visibility newVisibility, String comment) {
        if (model == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (model.getVisibility() == newVisibility) return model;

        updateLastVisChange(getHangarPrincipal().getUserId(), model.getId());

        insertNewVisibilityEntry(changeTableConstructor.create(getHangarPrincipal().getUserId(), comment == null ? "" : comment, newVisibility, model.getId()));

        Visibility oldVis = model.getVisibility();
        model.setVisibility(newVisibility);
        model = updateModel(model);
        model.logAction(actionLogger, visibilityChangeLogAction, newVisibility.getTitle(), oldVis.getTitle());
        postUpdate(model);
        return model;
    }

    public final M checkVisibility(@Nullable M model) {
        if (model == null) {
            return null;
        }
        logger.debug("Checking visibility of {} User: {}", model, SecurityContextHolder.getContext().getAuthentication());

        if (SecurityContextHolder.getContext().getAuthentication() instanceof JobService.JobAuthentication) {
            return model;
        }

        Permission perms = permissionService.getProjectPermissions(getHangarUserId(), model.getProjectId());
        if (!perms.has(Permission.SeeHidden) && !perms.has(Permission.IsProjectMember) && model.getVisibility() != Visibility.PUBLIC) {
            logger.debug("Not visible. Perms: {} Visibility: {}", perms, model.getVisibility());
            return null;
        }
        return model;
    }

    abstract void updateLastVisChange(long currentUserId, long modelId);

    abstract M getModel(long id);

    abstract M updateModel(M model);

    abstract void insertNewVisibilityEntry(VT visibilityTable);

    protected void postUpdate(@Nullable M model) { }

    abstract public Entry<String, VT> getLastVisibilityChange(long principalId);

    @FunctionalInterface
    interface VisibilityChangeTableConstructor<T extends VisibilityChangeTable> {

        T create(long createdBy, String comment, Visibility visibility, long subjectId);
    }

}

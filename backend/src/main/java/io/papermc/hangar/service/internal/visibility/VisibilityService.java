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
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

abstract class VisibilityService<LC extends LogContext<?, LC>, M extends Table & ModelVisible & ProjectIdentified & Loggable<LC>, VT extends VisibilityChangeTable> extends HangarComponent {

    @Autowired
    private PermissionService permissionService;

    private final VisibilityChangeTableConstructor<VT> changeTableConstructor;
    private final LogAction<LC> visibilityChangeLogAction;

    protected VisibilityService(final VisibilityChangeTableConstructor<VT> changeTableConstructor, final LogAction<LC> visibilityChangeLogAction) {
        this.changeTableConstructor = changeTableConstructor;
        this.visibilityChangeLogAction = visibilityChangeLogAction;
    }

    public M changeVisibility(final long modelId, final Visibility newVisibility, final String comment) {
        return this.changeVisibility(this.getModel(modelId), newVisibility, comment);
    }

    public M changeVisibility(@Nullable M model, final Visibility newVisibility, final String comment) {
        if (model == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (model.getVisibility() == newVisibility) {
            return model;
        }

        this.updateLastVisChange(this.getHangarPrincipal().getUserId(), model.getId());

        this.insertNewVisibilityEntry(this.changeTableConstructor.create(this.getHangarPrincipal().getUserId(), comment == null ? "" : comment, newVisibility, model.getId()));

        final Visibility oldVis = model.getVisibility();
        model.setVisibility(newVisibility);
        model = this.updateModel(model);
        model.logAction(this.actionLogger, this.visibilityChangeLogAction, newVisibility.getTitle(), oldVis.getTitle());
        this.postUpdate(model);
        return model;
    }

    public final M checkVisibility(final @Nullable M model) {
        if (model == null) {
            return null;
        }
        this.logger.debug("Checking visibility of {} User: {}", model, SecurityContextHolder.getContext().getAuthentication());

        if (SecurityContextHolder.getContext().getAuthentication() instanceof JobService.JobAuthentication) {
            return model;
        }

        final Permission perms = this.permissionService.getProjectPermissions(this.getHangarUserId(), model.getProjectId());
        if (!perms.has(Permission.SeeHidden) && !perms.has(Permission.IsProjectMember) && model.getVisibility() != Visibility.PUBLIC) {
            this.logger.debug("Not visible. Perms: {} Visibility: {}", perms, model.getVisibility());
            return null;
        }
        return model;
    }

    abstract void updateLastVisChange(long currentUserId, long modelId);

    abstract M getModel(long id);

    abstract M updateModel(M model);

    abstract void insertNewVisibilityEntry(VT visibilityTable);

    protected void postUpdate(final @Nullable M model) {
    }

    public abstract Map.Entry<String, VT> getLastVisibilityChange(long principalId);

    @FunctionalInterface
    interface VisibilityChangeTableConstructor<T extends VisibilityChangeTable> {

        T create(long createdBy, String comment, Visibility visibility, long subjectId);
    }

}

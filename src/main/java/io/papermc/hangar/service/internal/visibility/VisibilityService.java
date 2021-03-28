package io.papermc.hangar.service.internal.visibility;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.ModelVisible;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.ProjectIdentified;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.db.visibility.VisibilityChangeTable;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.PermissionService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Map.Entry;

public abstract class VisibilityService<M extends Table & ModelVisible & ProjectIdentified, VT extends VisibilityChangeTable> extends HangarService {

    @Autowired
    private PermissionService permissionService;

    private final VisibilityChangeTableConstructor<VT> changeTableConstructor;

    protected VisibilityService(VisibilityChangeTableConstructor<VT> changeTableConstructor) {
        this.changeTableConstructor = changeTableConstructor;
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

        model.setVisibility(newVisibility);
        model = updateModel(model);
        postUpdate();
        return model;
    }

    public final M checkVisibility(@Nullable M model) {
        if (model == null) {
            return null;
        }
        Permission perms = permissionService.getProjectPermissions(getHangarUserId(), model.getProjectId());
        if (!perms.has(Permission.SeeHidden) && !perms.has(Permission.IsProjectMember) && model.getVisibility() != Visibility.PUBLIC) {
            return null;
        }
        return model;
    }

    abstract void updateLastVisChange(long currentUserId, long modelId);

    abstract M getModel(long id);

    abstract M updateModel(M model);

    abstract void insertNewVisibilityEntry(VT visibilityTable);

    protected void postUpdate() { }

    abstract public Entry<String, VT> getLastVisibilityChange(long principalId);

    @FunctionalInterface
    interface VisibilityChangeTableConstructor<T extends VisibilityChangeTable> {

        T create(long createdBy, String comment, Visibility visibility, long subjectId);
    }

}

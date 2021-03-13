package io.papermc.hangar.service;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.VisibilityDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.model.ModelVisible;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.ProjectIdentified;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.visibility.ProjectVersionVisibilityChangeTable;
import io.papermc.hangar.model.db.visibility.ProjectVisibilityChangeTable;
import io.papermc.hangar.model.db.visibility.VisibilityChangeTable;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class VisibilityService<M extends Table & ModelVisible & ProjectIdentified, VT extends VisibilityChangeTable> extends HangarService {

    @Autowired
    private PermissionService permissionService;

    private final Consumer<VT> dbInsertion;
    private final BiConsumer<Long, Long> lastUpdater;
    private final VisibilityChangeTableConstructor<VT> changeTableConstructor;
    private final Function<M, M> modelUpdater;

    protected VisibilityService(Consumer<VT> dbInsertion, BiConsumer<Long, Long> lastUpdater, VisibilityChangeTableConstructor<VT> changeTableConstructor, Function<M, M> modelUpdater) {
        this.dbInsertion = dbInsertion;
        this.lastUpdater = lastUpdater;
        this.changeTableConstructor = changeTableConstructor;
        this.modelUpdater = modelUpdater;
    }

    public M changeVisibility(M model, Visibility newVisibility, String comment) {
        if (model.getVisibility() == newVisibility) return model;

        lastUpdater.accept(getHangarPrincipal().getUserId(), model.getId());

        dbInsertion.accept(changeTableConstructor.create(getHangarPrincipal().getUserId(), comment, newVisibility, model.getId()));

        model.setVisibility(newVisibility);
        return modelUpdater.apply(model);
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

    @FunctionalInterface
    private interface VisibilityChangeTableConstructor<T extends VisibilityChangeTable> {

        T create(long createdBy, String comment, Visibility visibility, long subjectId);
    }

    @Service
    public static class ProjectVisibilityService extends VisibilityService<ProjectTable, ProjectVisibilityChangeTable> {

        @Autowired
        public ProjectVisibilityService(HangarDao<VisibilityDAO> visibilityDAO, HangarDao<ProjectsDAO> projectsDAO) {
            super(visibilityDAO.get()::insert, visibilityDAO.get()::updateLatestProjectChange, ProjectVisibilityChangeTable::new, projectsDAO.get()::update);
        }

        @Override
        public ProjectTable changeVisibility(ProjectTable model, Visibility newVisibility, String comment) {
            Visibility oldVis = model.getVisibility();
            // TODO add logging for visibility for versions and move this to the abstract class
            userActionLogService.project(LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(model.getId())), newVisibility.getName(), oldVis.getName());
            return super.changeVisibility(model, newVisibility, comment);
        }
    }

    @Service
    public static class ProjectVersionVisibilityService extends VisibilityService<ProjectVersionTable, ProjectVersionVisibilityChangeTable> {

        @Autowired
        public ProjectVersionVisibilityService(HangarDao<VisibilityDAO> visibilityDAO, HangarDao<ProjectVersionsDAO> projectVersionDAO) {
            super(visibilityDAO.get()::insert, visibilityDAO.get()::updateLatestVersionChange, ProjectVersionVisibilityChangeTable::new, projectVersionDAO.get()::update);
        }
    }
}

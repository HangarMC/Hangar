package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.ProjectDAO;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.VisibilityService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class ProjectService extends HangarService {

    public static final String AUTHOR = "author";
    public static final String SLUG = "slug";

    private final ProjectDAO projectDAO;
    private final VisibilityService visibilityService;
    private final OrganizationService organizationService;

    @Autowired
    public ProjectService(HangarDao<ProjectDAO> projectDAO, VisibilityService visibilityService, OrganizationService organizationService) {
        this.projectDAO = projectDAO.get();
        this.visibilityService = visibilityService;
        this.organizationService = organizationService;
    }

    @Nullable
    public ProjectTable getProjectTable(@Nullable Long projectId) {
        return getProjectTable(projectId, projectDAO::getById);
    }

    public ProjectTable getProjectTable(@Nullable String author, @Nullable String name) {
        return getProjectTable(author, name, projectDAO::getBySlug);
    }

    @Nullable
    public ProjectOwner getProjectOwner(long userId) {
        if (Objects.equals(getHangarUserId(), userId)) {
            return getHangarPrincipal();
        }
        return organizationService.getOrganizationTablesWithPermission(userId, Permission.CreateProject).stream().filter(ot -> ot.getUserId() == userId).findFirst().orElse(null);
    }

    @Nullable
    private <T> ProjectTable getProjectTable(@Nullable T identifier, @NotNull Function<T, ProjectTable> projectTableFunction) {
        if (identifier == null) {
            return null;
        }
        return visibilityService.checkVisibility(projectTableFunction.apply(identifier), ProjectTable::getId);
    }

    @Nullable
    private <T> ProjectTable getProjectTable(@Nullable T identifierOne, @Nullable T identifierTwo, @NotNull BiFunction<T, T, ProjectTable> projectTableFunction) {
        if (identifierOne == null || identifierTwo == null) {
            return null;
        }
        return visibilityService.checkVisibility(projectTableFunction.apply(identifierOne, identifierTwo), ProjectTable::getId);
    }
}

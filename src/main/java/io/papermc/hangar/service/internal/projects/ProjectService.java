package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.HangarUsersDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectDAO;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.HangarProject;
import io.papermc.hangar.model.internal.HangarProjectFlag;
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.VisibilityService;
import io.papermc.hangar.service.internal.OrganizationService;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class ProjectService extends HangarService {

    public static final String AUTHOR = "author";
    public static final String SLUG = "slug";

    private final ProjectDAO projectDAO;
    private final HangarUsersDAO hangarUsersDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final VisibilityService visibilityService;
    private final OrganizationService organizationService;

    @Autowired
    public ProjectService(HangarDao<ProjectDAO> projectDAO, HangarDao<HangarUsersDAO> hangarUsersDAO, HangarDao<HangarProjectsDAO> hangarProjectsDAO, VisibilityService visibilityService, OrganizationService organizationService) {
        this.projectDAO = projectDAO.get();
        this.hangarUsersDAO = hangarUsersDAO.get();
        this.hangarProjectsDAO = hangarProjectsDAO.get();
        this.visibilityService = visibilityService;
        this.organizationService = organizationService;
    }

    @Nullable
    public ProjectTable getProjectTable(@Nullable Long projectId) {
        return getProjectTable(projectId, projectDAO::getById);
    }

    public ProjectTable getProjectTable(@Nullable String author, @Nullable String slug) {
        return getProjectTable(author, slug, projectDAO::getBySlug);
    }

    @Nullable
    public ProjectOwner getProjectOwner(long userId) {
        if (Objects.equals(getHangarUserId(), userId)) {
            return getHangarPrincipal();
        }
        return organizationService.getOrganizationTablesWithPermission(userId, Permission.CreateProject).stream().filter(ot -> ot.getUserId() == userId).findFirst().orElse(null);
    }

    public ProjectOwner getProjectOwner(String userName) {
        Pair<UserTable, OrganizationTable> pair = hangarUsersDAO.getUserAndOrg(userName);
        if (pair.getRight() != null) {
            return pair.getRight();
        }
        return pair.getLeft();
    }

    public HangarProject getHangarProject(String author, String slug) {
        Pair<Long, Project> project = hangarProjectsDAO.getProject(author, slug);
        ProjectOwner projectOwner = getProjectOwner(author);
        List<JoinableMember<ProjectRoleTable>> members = hangarProjectsDAO.getProjectMembers(project.getLeft());
        // only include visibility change if not public (and if so, only include the user and comment)
        HangarProject.HangarProjectInfo info = hangarProjectsDAO.getHangarProjectInfo(project.getLeft());
        return new HangarProject(project.getRight(), projectOwner, members, "", "", info);
    }

    public List<HangarProjectFlag> getHangarProjectFlags(String author, String slug) {
        return hangarProjectsDAO.getHangarProjectFlags(author, slug);
    }

    public void refreshHomeProjects() {
        hangarProjectsDAO.refreshHomeProjects();
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

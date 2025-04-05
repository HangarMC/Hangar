package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.index.MeiliService;
import io.papermc.hangar.components.index.MeiliService.SearchResult;
import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectQueryFilter.ProjectQueryFilterInstance;
import io.papermc.hangar.db.dao.v1.ProjectsApiDAO;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.projects.ProjectTable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectsApiService extends HangarComponent {

    private final ProjectsApiDAO projectsApiDAO;
    private final MeiliService meiliService;

    @Autowired
    public ProjectsApiService(final ProjectsApiDAO projectsApiDAO, final MeiliService meiliService) {
        this.projectsApiDAO = projectsApiDAO;
        this.meiliService = meiliService;
    }

    public Project getProject(final long id) {
        final boolean seeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final Project project = this.projectsApiDAO.getProject(id, seeHidden, this.getHangarUserId());
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project " + id + " not found");
        }
        return project;
    }

    public Project getProjectForVersionHash(final String fileHash) {
        final boolean seeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final Long projectId = this.projectsApiDAO.getProjectIdFromVersionHash(fileHash.toLowerCase(Locale.ROOT), seeHidden, this.getHangarUserId());
        if (projectId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No project found for version hash " + fileHash);
        }
        return this.getProject(projectId);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<ProjectMember> getProjectMembers(final ProjectTable project, final RequestPagination requestPagination) {
        final List<ProjectMember> projectMembers = this.projectsApiDAO.getProjectMembers(project.getId(), requestPagination);
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectMembersCount(project.getId()), requestPagination), projectMembers);
    }

    public Map<String, DayProjectStats> getProjectStats(final ProjectTable project, final OffsetDateTime fromDate, final OffsetDateTime toDate) {
        return this.projectsApiDAO.getProjectStats(project.getId(), fromDate, toDate);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<User> getProjectStargazers(final ProjectTable project, final RequestPagination pagination) {
        final List<User> stargazers = this.projectsApiDAO.getProjectStargazers(project.getId(), pagination.getLimit(), pagination.getOffset());
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectStargazersCount(project.getId()), pagination), stargazers);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<User> getProjectWatchers(final ProjectTable project, final RequestPagination pagination) {
        final List<User> watchers = this.projectsApiDAO.getProjectWatchers(project.getId(), pagination.getLimit(), pagination.getOffset());
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectWatchersCount(project.getId()), pagination), watchers);
    }

    public PaginatedResult<Project> getProjects(final RequestPagination pagination) {
        String query = "";
        final StringBuilder filters = new StringBuilder();
        boolean first = true;
        for (final Filter.FilterInstance filterInstance : pagination.getFilters().values()) {
            if (filterInstance instanceof ProjectQueryFilterInstance(String q)) {
                query = q.toLowerCase();
            } else {
                if (first) {
                    first = false;
                } else {
                    filters.append(" AND ");
                }
                filterInstance.createMeili(filters);
            }
        }

        final List<String> sort = new ArrayList<>();
        pagination.getSorters().forEach((key, value) -> {
            StringBuilder sb = new StringBuilder();
            value.accept(sb);
            sort.add(sb.toString());
        });

        SearchResult<Project> result = this.meiliService.searchProjects(query, filters.toString(), sort, pagination.getOffset(), pagination.getLimit());
        return result.asPaginatedResult();
    }
}

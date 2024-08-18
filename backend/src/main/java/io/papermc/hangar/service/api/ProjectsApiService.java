package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectQueryFilter;
import io.papermc.hangar.db.dao.v1.ProjectsApiDAO;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectsApiService extends HangarComponent {

    private final ProjectsApiDAO projectsApiDAO;
    private final UsersApiService usersApiService;

    @Autowired
    public ProjectsApiService(final ProjectsApiDAO projectsApiDAO, final UsersApiService usersApiService) {
        this.projectsApiDAO = projectsApiDAO;
        this.usersApiService = usersApiService;
    }

    public Project getProject(final String slug) {
        final boolean seeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final Project project = this.projectsApiDAO.getProject(slug, seeHidden, this.getHangarUserId());
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project " + slug + " not found");
        }
        return project;
    }

    public Project getProjectForVersionHash(final String fileHash) {
        final boolean seeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final String projectSlug = this.projectsApiDAO.getProjectSlugFromVersionHash(fileHash.toLowerCase(Locale.ROOT), seeHidden, this.getHangarUserId());
        if (projectSlug == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No project found for version hash " + fileHash);
        }
        return this.getProject(projectSlug);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<ProjectMember> getProjectMembers(final String slug, final RequestPagination requestPagination) {
        final List<ProjectMember> projectMembers = this.projectsApiDAO.getProjectMembers(slug, requestPagination);
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectMembersCount(slug), requestPagination), projectMembers);
    }

    public Map<String, DayProjectStats> getProjectStats(final String slug, final OffsetDateTime fromDate, final OffsetDateTime toDate) {
        return this.projectsApiDAO.getProjectStats(slug, fromDate, toDate);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<User> getProjectStargazers(final String slug, final RequestPagination pagination) {
        final List<User> stargazers = this.projectsApiDAO.getProjectStargazers(slug, pagination.getLimit(), pagination.getOffset());
        // TODO rewrite avatar fetching
        stargazers.forEach(this.usersApiService::supplyAvatarUrl);
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectStargazersCount(slug), pagination), stargazers);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<User> getProjectWatchers(final String slug, final RequestPagination pagination) {
        final List<User> watchers = this.projectsApiDAO.getProjectWatchers(slug, pagination.getLimit(), pagination.getOffset());
        // TODO rewrite avatar fetching
        watchers.forEach(this.usersApiService::supplyAvatarUrl);
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectWatchersCount(slug), pagination), watchers);
    }

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.PROJECTS)
    public PaginatedResult<Project> getProjects(final RequestPagination pagination, final boolean seeHidden, final boolean prioritizeExactMatch) {
        // get query from filter
        String query = null;
        for (final Filter.FilterInstance filterInstance : pagination.getFilters().values()) {
            if (filterInstance instanceof final ProjectQueryFilter.ProjectQueryFilterInstance queryFilter) {
                query = queryFilter.query().toLowerCase();
            }
        }

        if (prioritizeExactMatch && query != null && !query.isBlank()) {
            // This sorter needs to be first
            final Map<String, Consumer<StringBuilder>> sorters = pagination.getSorters();
            final Map<String, Consumer<StringBuilder>> copy = new LinkedHashMap<>(sorters);
            sorters.clear();
            sorters.put("exact_match", sb -> sb.append(" exact_match ASC"));
            sorters.putAll(copy);
        }

        final Long userId = this.getHangarUserId();
        final List<Project> projects = this.projectsApiDAO.getProjects(seeHidden, userId, pagination, query);
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.countProjects(seeHidden, userId, pagination), pagination), projects);
    }
}

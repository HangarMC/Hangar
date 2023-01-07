package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
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
import java.util.List;
import java.util.Map;
import io.papermc.hangar.service.internal.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectsApiService extends HangarComponent {

    private final ProjectsApiDAO projectsApiDAO;
    private final UsersApiService usersApiService;
    private final AvatarService avatarService;

    @Autowired
    public ProjectsApiService(final ProjectsApiDAO projectsApiDAO, final UsersApiService usersApiService, final AvatarService avatarService) {
        this.projectsApiDAO = projectsApiDAO;
        this.usersApiService = usersApiService;
        this.avatarService = avatarService;
    }

    public Project getProject(final String author, final String slug) {
        final boolean seeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final Project project = this.projectsApiDAO.getProject(author, slug, seeHidden, this.getHangarUserId());
        project.setAvatarUrl(this.avatarService.getProjectAvatarUrl(project.getId(), project.getNamespace().getOwner()));
        return project;
    }

    @Transactional
    public PaginatedResult<ProjectMember> getProjectMembers(final String author, final String slug, final RequestPagination requestPagination) {
        final List<ProjectMember> projectMembers = this.projectsApiDAO.getProjectMembers(author, slug, requestPagination);
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectMembersCount(author, slug), requestPagination), projectMembers);
    }

    public Map<String, DayProjectStats> getProjectStats(final String author, final String slug, final OffsetDateTime fromDate, final OffsetDateTime toDate) {
        return this.projectsApiDAO.getProjectStats(author, slug, fromDate, toDate);
    }

    @Transactional
    public PaginatedResult<User> getProjectStargazers(final String author, final String slug, final RequestPagination pagination) {
        final List<User> stargazers = this.projectsApiDAO.getProjectStargazers(author, slug, pagination.getLimit(), pagination.getOffset());
        stargazers.forEach(this.usersApiService::supplyAvatarUrl);
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectStargazersCount(author, slug), pagination), stargazers);
    }

    @Transactional
    public PaginatedResult<User> getProjectWatchers(final String author, final String slug, final RequestPagination pagination) {
        final List<User> watchers = this.projectsApiDAO.getProjectWatchers(author, slug, pagination.getLimit(), pagination.getOffset());
        watchers.forEach(this.usersApiService::supplyAvatarUrl);
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.getProjectWatchersCount(author, slug), pagination), watchers);
    }

    @Transactional
    @Cacheable(CacheConfig.PROJECTS)
    public PaginatedResult<Project> getProjects(final String query, final boolean orderWithRelevance, final RequestPagination pagination, final boolean seeHidden) {
        String relevance = "";
        if (orderWithRelevance && query != null && !query.isEmpty()) {
            if (query.endsWith(" ")) {
                relevance = "ts_rank(hp.search_words, websearch_to_tsquery('english', :query)) AS relevance,";
            } else {
                relevance = "ts_rank(hp.search_words, websearch_to_tsquery_postfix('english', :query)) AS relevance,";
            }
            pagination.getSorters().put("relevance", sb -> sb.append(" relevance DESC"));
        }

        final List<Project> projects = this.projectsApiDAO.getProjects(seeHidden, this.getHangarUserId(), pagination, relevance);
        projects.forEach(p -> p.setAvatarUrl(this.avatarService.getProjectAvatarUrl(p.getId(), p.getNamespace().getOwner())));
        return new PaginatedResult<>(new Pagination(this.projectsApiDAO.countProjects(seeHidden, this.getHangarUserId(), pagination), pagination), projects);
    }
}

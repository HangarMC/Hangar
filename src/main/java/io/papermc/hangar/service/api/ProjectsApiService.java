package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.db.dao.v1.ProjectsApiDAO;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectsApiService extends HangarComponent {

    private final ProjectsApiDAO projectsApiDAO;

    @Autowired
    public ProjectsApiService(ProjectsApiDAO projectsApiDAO) {
        this.projectsApiDAO = projectsApiDAO;
    }

    public Project getProject(String author, String slug) {
        boolean seeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        return projectsApiDAO.getProject(author, slug, seeHidden, getHangarUserId());
    }

    public PaginatedResult<ProjectMember> getProjectMembers(String author, String slug, RequestPagination requestPagination) {
        List<ProjectMember> projectMembers = projectsApiDAO.getProjectMembers(author, slug, requestPagination);
        return new PaginatedResult<>(new Pagination(projectsApiDAO.getProjectMembersCount(author, slug), requestPagination), projectMembers);
    }

    public Map<String, DayProjectStats> getProjectStats(String author, String slug, OffsetDateTime fromDate, OffsetDateTime toDate) {
        return projectsApiDAO.getProjectStats(author, slug, fromDate, toDate);
    }

    public PaginatedResult<User> getProjectStargazers(String author, String slug, RequestPagination pagination) {
        List<User> stargazers = projectsApiDAO.getProjectStargazers(author, slug, pagination.getLimit(), pagination.getOffset());
        return new PaginatedResult<>(new Pagination(projectsApiDAO.getProjectStargazersCount(author, slug), pagination), stargazers);
    }

    public PaginatedResult<User> getProjectWatchers(String author, String slug, RequestPagination pagination) {
        List<User> watchers = projectsApiDAO.getProjectWatchers(author, slug, pagination.getLimit(), pagination.getOffset());
        return new PaginatedResult<>(new Pagination(projectsApiDAO.getProjectWatchersCount(author, slug), pagination), watchers);
    }

    public PaginatedResult<Project> getProjects(String query, boolean orderWithRelevance, RequestPagination pagination) {
        String relevance = "";
        if (orderWithRelevance && query != null && !query.isEmpty()) {
            if(query.endsWith(" ")) {
                relevance = "ts_rank(hp.search_words, websearch_to_tsquery('english', :query)) AS relevance,";
            } else {
                relevance = "ts_rank(hp.search_words, websearch_to_tsquery_postfix('english', :query)) AS relevance,";
            }
            pagination.getSorters().put("relevance", sb -> sb.append(" relevance DESC"));
        }

        boolean seeHidden = getGlobalPermissions().has(Permission.SeeHidden);

        List<Project> projects = projectsApiDAO.getProjects(seeHidden, getHangarUserId(), pagination, relevance);
        return new PaginatedResult<>(new Pagination(projectsApiDAO.countProjects(seeHidden, getHangarUserId(), pagination), pagination), projects);
    }
}

package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
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
import java.util.List;
import java.util.Map;

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

    public PaginatedResult<Project> getProjects(String query, ProjectSortingStrategy sort, boolean orderWithRelevance, RequestPagination pagination) {

        String ordering = sort.getSql();
        if (orderWithRelevance && query != null && !query.isEmpty()) {
            String relevance = "ts_rank(hp.search_words, websearch_to_tsquery_postfix('english', :query)) DESC";
            if(query.endsWith(" ")) {
                relevance = "ts_rank(hp.search_words, websearch_to_tsquery('english', :query)) DESC";
            }
            relevance = "ASC";
            String orderingFirstHalf;
            // 1609459200 is the hangar epoch
            // 86400 seconds to days
            // 604800‬ seconds to weeks
            switch(sort){
                case STARS: orderingFirstHalf = "hp.stars * "; break;
                case DOWNLOADS: orderingFirstHalf ="(hp.downloads / 100) * "; break;
                case VIEWS: orderingFirstHalf ="(hp.views / 200) *"; break;
                case NEWEST: orderingFirstHalf ="((EXTRACT(EPOCH FROM hp.created_at) - 1609459200) / 86400) *"; break;
                case UPDATED: orderingFirstHalf ="last_updated_double "; break;
                case ONLY_RELEVANCE: orderingFirstHalf = ""; break;
                case RECENT_DOWNLOADS : orderingFirstHalf ="hp.recent_views *"; break;
                case RECENT_VIEWS: orderingFirstHalf ="hp.recent_downloads *"; break;
                default:
                    orderingFirstHalf = " "; // Just in case and so that the ide doesn't complain
            }
            ordering = orderingFirstHalf + relevance;

            System.out.println("Ordering First Half: " + orderingFirstHalf);
            System.out.println("Ordering Relevance: " + relevance);
        }

        boolean seeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        System.out.println("See Hidden: " + seeHidden);
        System.out.println("User ID: " + getHangarUserId());
        System.out.println("Ordering: " + ordering);


        System.out.println("Pagination: " + pagination);

        List<Project> projects = projectsApiDAO.getProjects(seeHidden, getHangarUserId(), ordering, pagination);
        for(Project project : projects){
            System.out.println("Returned " + project.getName());
        }
        return new PaginatedResult<>(new Pagination(projectsApiDAO.countProjects(seeHidden, getHangarUserId(), pagination), pagination), projects);
    }
}

package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
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
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProjectsApiService extends HangarService {

    private final ProjectsApiDAO projectsApiDAO;

    @Autowired
    public ProjectsApiService(HangarDao<ProjectsApiDAO> projectsApiDAO) {
        this.projectsApiDAO = projectsApiDAO.get();
    }

    public Project getProject(String author, String slug) {
        boolean seeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        return projectsApiDAO.getProject(author, slug, seeHidden, getHangarUserId());
    }

    public PaginatedResult<ProjectMember> getProjectMembers(String author, String slug, RequestPagination requestPagination) {
        List<ProjectMember> projectMembers = projectsApiDAO.getProjectMembers(author, slug, requestPagination.getLimit(), requestPagination.getOffset());
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
            String orderingFirstHalf;
            // 1483056000 is the Ore epoch TODO change to hangar epoch
            // 86400 seconds to days
            // 604800‬ seconds to weeks
            switch(sort){
                case STARS: orderingFirstHalf = "hp.stars * "; break;
                case DOWNLOADS: orderingFirstHalf ="(hp.downloads / 100) * "; break;
                case VIEWS: orderingFirstHalf ="(hp.views / 200) *"; break;
                case NEWEST: orderingFirstHalf ="((EXTRACT(EPOCH FROM hp.created_at) - 1483056000) / 86400) *"; break;
                case UPDATED: orderingFirstHalf ="((EXTRACT(EPOCH FROM hp.last_updated) - 1483056000) / 604800) *"; break;
                case ONLY_RELEVANCE: orderingFirstHalf = ""; break;
                case RECENT_DOWNLOADS : orderingFirstHalf ="hp.recent_views *"; break;
                case RECENT_VIEWS: orderingFirstHalf ="hp.recent_downloads *"; break;
                default:
                    orderingFirstHalf = " "; // Just in case and so that the ide doesnt complain
            }
            ordering = orderingFirstHalf + relevance;
        }

        boolean seeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        List<Project> projects = projectsApiDAO.getProjects(seeHidden, getHangarUserId(), ordering, pagination);
        return new PaginatedResult<>(new Pagination(projectsApiDAO.countProjects(seeHidden, getHangarUserId(), pagination), pagination), projects);
    }
}

package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.db.dao.v1.ProjectsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        SorterRegistry sorterObject = SorterRegistry.VIEWS; //Views = default sorting mode

        String sorterName = null;
        if(pagination.getSorters().size() > 0){
            sorterName = (String) pagination.getSorters().keySet().toArray()[0];
        }
        if(sorterName != null){
            Set<String> applicableSorters = Arrays.stream(SorterRegistry.values()).map(SorterRegistry::getName).collect(Collectors.toSet());
            String sortKey = sorterName.startsWith("-") ? sorterName.substring(1) : sorterName;
            if (applicableSorters.contains(sortKey)) {
                sorterObject = SorterRegistry.getSorter(sortKey);
            }
        }



        StringBuilder sql = new StringBuilder();
        sorterObject.descending().accept(sql);


        boolean seeHidden = getGlobalPermissions().has(Permission.SeeHidden);


        List<Project> projects = projectsApiDAO.getProjects(seeHidden, getHangarUserId(), sql.toString(), pagination);
        return new PaginatedResult<>(new Pagination(projectsApiDAO.countProjects(seeHidden, getHangarUserId(), pagination), pagination), projects);


       /* String ordering = sort.getSql();

        System.out.println("PSS: " + sort.toString());

        String paginationSortValue = "mostStars";
        if(paginationSortValue != null){
            if(paginationSortValue.equalsIgnoreCase("mostStars")) {
                sort = ProjectSortingStrategy.STARS;
            } else if(paginationSortValue.equalsIgnoreCase("mostDownloads")) {
                sort = ProjectSortingStrategy.DOWNLOADS;
            } else if(paginationSortValue.equalsIgnoreCase("mostViews")) {
                sort = ProjectSortingStrategy.VIEWS;
            } else if(paginationSortValue.equalsIgnoreCase("newest")) {
                sort = ProjectSortingStrategy.NEWEST;
            } else if(paginationSortValue.equalsIgnoreCase("recentlyUpdated")) {
                sort = ProjectSortingStrategy.UPDATED;
            }
        }*/



/*
        if (orderWithRelevance && query != null && !query.isEmpty()) {
            String relevance = "ts_rank(hp.search_words, websearch_to_tsquery_postfix('english', :query)) DESC";
            if(query.endsWith(" ")) {
                relevance = "ts_rank(hp.search_words, websearch_to_tsquery('english', :query)) DESC";
            }
            relevance = "ASC";
            String orderingFirstHalf;
            // 1609459200 is the hangar epoch
            // 86400 seconds to days
            // 604800 seconds to weeks
            switch(sort){
                case STARS: orderingFirstHalf = "hp.stars * "; relevance="DESC"; break;
                case DOWNLOADS: orderingFirstHalf ="(hp.downloads / 100) * "; relevance="DESC"; break;
                case VIEWS: orderingFirstHalf ="(hp.views / 200) *"; relevance="DESC"; break;
                //case NEWEST: orderingFirstHalf ="((EXTRACT(EPOCH FROM hp.created_at) - 1609459200) / 86400) *"; break;
                case NEWEST: orderingFirstHalf ="newest_double "; relevance="ASC"; break;
                case UPDATED: orderingFirstHalf ="last_updated_double "; break;
                case ONLY_RELEVANCE: orderingFirstHalf = ""; break;
                case RECENT_DOWNLOADS : orderingFirstHalf ="hp.recent_views *"; break;
                case RECENT_VIEWS: orderingFirstHalf ="hp.recent_downloads *"; break;
                default:
                    orderingFirstHalf = " "; // Just in case and so that the ide doesn't complain
            }
            ordering = orderingFirstHalf + relevance;
        }

        if(paginationSortValue != null && !paginationSortValue.isBlank()){
            String relevance = "ASC";
            String orderingFirstHalf;

            switch (sort) {
                case STARS -> {
                    orderingFirstHalf = "hp.stars ";
                    relevance = "DESC";
                }
                case DOWNLOADS -> {
                    orderingFirstHalf = "hp.downloads ";
                    relevance = "DESC";
                }
                case VIEWS -> {
                    orderingFirstHalf = "hp.views ";
                    relevance = "DESC";
                }
                //case NEWEST: orderingFirstHalf ="((EXTRACT(EPOCH FROM hp.created_at) - 1609459200) / 86400) *"; break;
                case NEWEST -> {
                    orderingFirstHalf = "newest_double ";
                    relevance = "DESC";
                }
                case UPDATED -> {
                    orderingFirstHalf = "last_updated_double ";
                    relevance = "DESC";
                }
                case ONLY_RELEVANCE -> orderingFirstHalf = "";
                case RECENT_DOWNLOADS -> orderingFirstHalf = "hp.recent_views ";
                case RECENT_VIEWS -> orderingFirstHalf = "hp.recent_downloads ";
                default -> orderingFirstHalf = " "; // Just in case and so that the ide doesn't complain
            }
            ordering = orderingFirstHalf + relevance;

        }

        boolean seeHidden = getGlobalPermissions().has(Permission.SeeHidden);

        //System.out.println("Ordering: " + ordering + " Sort: " + sort + " PSV: " + paginationSortValue);

        List<Project> projects = projectsApiDAO.getProjects(seeHidden, getHangarUserId(), ordering, pagination);
        return new PaginatedResult<>(new Pagination(projectsApiDAO.countProjects(seeHidden, getHangarUserId(), pagination), pagination), projects);*/
    }
}

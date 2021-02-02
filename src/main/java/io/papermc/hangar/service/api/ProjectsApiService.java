package io.papermc.hangar.service.api;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.extras.HangarApiRequest;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.v1.ProjectsApiDAO;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.modelold.generated.ProjectSortingStrategy;
import io.papermc.hangar.modelold.generated.Tag;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.serviceold.pluginupload.ProjectFiles;
import io.papermc.hangar.util.ApiUtil;
import io.papermc.hangar.util.Routes;
import io.papermc.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectsApiService extends HangarService {

    private final HangarConfig hangarConfig;
    private final ProjectsApiDAO projectsApiDAO;
    private final ProjectFiles projectFiles;

    private final HangarApiRequest hangarApiRequest;

    @Autowired
    public ProjectsApiService(HangarConfig hangarConfig, HangarDao<ProjectsApiDAO> projectsDAO, ProjectFiles projectFiles, HangarApiRequest hangarApiRequest) {
        this.hangarConfig = hangarConfig;
        this.projectsApiDAO = projectsDAO.get();
        this.projectFiles = projectFiles;
        this.hangarApiRequest = hangarApiRequest;
    }

    public Project getProject(String author, String slug) {
        boolean seeHidden = hangarApiRequest.getGlobalPermissions().has(Permission.SeeHidden);
        return projectsApiDAO.getProjects(author, slug, seeHidden, hangarApiRequest.getUserId(), null, null, null, null, null, 1, 0).stream().findFirst().map(this::setProjectIconUrl).orElse(null);
    }

    public PaginatedResult<ProjectMember> getProjectMembers(String author, String slug, RequestPagination requestPagination) {
        List<ProjectMember> projectMembers = projectsApiDAO.getProjectMembers(author, slug, requestPagination.getLimit(), requestPagination.getOffset());
        return new PaginatedResult<>(new Pagination(projectsApiDAO.getProjectMembersCount(author, slug), requestPagination), projectMembers);
    }

    public PaginatedResult<Project> getProjects(String query, List<Category> categories, List<String> tags, String owner, ProjectSortingStrategy sort, boolean orderWithRelevance, RequestPagination pagination) {
        List<Tag> parsedTags = new ArrayList<>();
        if (tags == null) {
            tags = new ArrayList<>();
        }
        for (String tag : tags) {
            String[] split = tag.split(":", 2);
            parsedTags.add(new Tag().name(split[0]).data(split.length > 1 ? split[1] : null));
        }
        boolean seeHidden = hangarApiRequest.getGlobalPermissions().has(Permission.SeeHidden);

        String ordering = ApiUtil.strategyOrDefault(sort).getSql();
        if (orderWithRelevance && query != null && !query.isEmpty()) {
            String relevance = "ts_rank(p.search_words, websearch_to_tsquery_postfix('english', :query)) DESC";
            if(query.endsWith(" ")) {
                relevance = "ts_rank(p.search_words, websearch_to_tsquery('english', :query)) DESC";
            }
            String orderingFirstHalf;
            // 1483056000 is the Ore epoch TODO change to hangar epoch
            // 86400 seconds to days
            // 604800‬ seconds to weeks
            switch(ApiUtil.strategyOrDefault(sort)){
                case STARS: orderingFirstHalf = "p.starts * "; break;
                case DOWNLOADS: orderingFirstHalf ="(p.downloads / 100) * "; break;
                case VIEWS: orderingFirstHalf ="(p.views / 200) *"; break;
                case NEWEST: orderingFirstHalf ="((EXTRACT(EPOCH FROM p.created_at) - 1483056000) / 86400) *"; break;
                case UPDATED: orderingFirstHalf ="((EXTRACT(EPOCH FROM p.last_updated) - 1483056000) / 604800) *"; break;
                case ONLY_RELEVANCE: orderingFirstHalf =""; break;
                case RECENT_DOWNLOADS : orderingFirstHalf ="p.recent_views *"; break;
                case RECENT_VIEWS: orderingFirstHalf ="p.recent_downloads*"; break;
                default:
                    orderingFirstHalf = " "; // Just in case and so that the ide doesnt complain
            }
            ordering = orderingFirstHalf + relevance;
        }

        List<Project> projects = projectsApiDAO.getProjects(owner, null, seeHidden, hangarApiRequest.getUserId(), ordering, getCategoryNumbers(categories), getTagsNames(parsedTags), trimQuery(query), getQueryStatement(query), pagination.getLimit(), pagination.getOffset());
        projects.forEach(this::setProjectIconUrl);
        return new PaginatedResult<>(new Pagination(projectsApiDAO.countProjects(owner, null, seeHidden, hangarApiRequest.getUserId(), getCategoryNumbers(categories), getTagsNames(parsedTags), trimQuery(query), getQueryStatement(query)), pagination), projects);
    }

    private List<Integer> getCategoryNumbers(List<Category> categories){
        return categories == null ? null : categories.stream().map(Category::getValue).collect(Collectors.toList());
    }

    private List<String> getTagsNames(List<Tag> tags){
        return tags == null ? null : tags.stream().filter(tag -> tag.getData() == null).map(Tag::getName).collect(Collectors.toList());
    }

    private String trimQuery(String query){
        String trimmedQuery = null;
        if(query != null && !query.isBlank()) {
            trimmedQuery = query.trim(); // Ore#APIV2Queries line 169 && 200
        }
        return trimmedQuery;
    }

    private String getQueryStatement(String query){
        String queryStatement = null;
        if(query != null && !query.isBlank()){
            if(query.endsWith(" ")) {
                queryStatement = "p.search_words @@ websearch_to_tsquery('english', :query)";
            } else {
                queryStatement =  "p.search_words @@ websearch_to_tsquery_postfix('english', :query)";
            }
        }
        return queryStatement;
    }

    private Project setProjectIconUrl(Project project) {
        Path iconPath = projectFiles.getIconPath(project.getNamespace().getOwner(), project.getNamespace().getSlug());
        if (iconPath != null) {
            project.setIconUrl(hangarConfig.getBaseUrl() + Routes.getRouteUrlOf("projects.showIcon", project.getNamespace().getOwner(), project.getNamespace().getSlug()));
        } else {
            project.setIconUrl(StringUtils.avatarUrl(project.getNamespace().getOwner()));
        }
        return project;
    }
}

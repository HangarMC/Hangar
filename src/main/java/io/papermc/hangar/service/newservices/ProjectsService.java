package io.papermc.hangar.service.newservices;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.requestmodels.api.RequestPagination;
import io.papermc.hangar.db.dao.ProjectsDAO;
import io.papermc.hangar.db.daoold.HangarDao;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.modelold.generated.ProjectSortingStrategy;
import io.papermc.hangar.modelold.generated.Tag;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
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
public class ProjectsService {

    private final HangarConfig hangarConfig;
    private final ProjectsDAO projectsDAO;
    private final ProjectFiles projectFiles;

    private final ApiAuthInfo apiAuthInfo;

    @Autowired
    public ProjectsService(HangarConfig hangarConfig, HangarDao<ProjectsDAO> projectsDAO, ProjectFiles projectFiles, ApiAuthInfo apiAuthInfo) {
        this.hangarConfig = hangarConfig;
        this.projectsDAO = projectsDAO.get();
        this.projectFiles = projectFiles;
        this.apiAuthInfo = apiAuthInfo;
    }

    public <T extends Project> T getProject(String author, String slug, Class<T> type) {
        boolean seeHidden = apiAuthInfo.getGlobalPerms().has(Permission.SeeHidden);
        return projectsDAO.getProjects(author, slug, seeHidden, apiAuthInfo.getUserId(), null, null, null, null, null, 1, 0, type).stream().findFirst().orElse(null);
    }

    public <T extends Project> PaginatedResult<T> getProjects(String query, List<Category> categories, List<String> tags, String owner, ProjectSortingStrategy sort, boolean orderWithRelevance, RequestPagination pagination, Class<T> type) {
        List<Tag> parsedTags = new ArrayList<>();
        if (tags == null) {
            tags = new ArrayList<>();
        }
        for (String tag : tags) {
            String[] split = tag.split(":", 2);
            parsedTags.add(new Tag().name(split[0]).data(split.length > 1 ? split[1] : null));
        }
        boolean seeHidden = apiAuthInfo.getGlobalPerms().has(Permission.SeeHidden);

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

        List<T> projects = projectsDAO.getProjects(owner, null, seeHidden, apiAuthInfo.getUserId(), ordering, getCategoryNumbers(categories), getTagsNames(parsedTags), trimQuery(query), getQueryStatement(query), pagination.getLimit(), pagination.getOffset(), type);
        projects.forEach(this::setProjectIconUrl);
        return new PaginatedResult<>(new Pagination(projectsDAO.countProjects(owner, null, seeHidden, apiAuthInfo.getUserId(), getCategoryNumbers(categories), getTagsNames(parsedTags), trimQuery(query), getQueryStatement(query)), pagination), projects);
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

    private void setProjectIconUrl(Project project) {
        Path iconPath = projectFiles.getIconPath(project.getNamespace().getOwner(), project.getNamespace().getSlug());
        if (iconPath != null) {
            project.setIconUrl(hangarConfig.getBaseUrl() + Routes.getRouteUrlOf("projects.showIcon", project.getNamespace().getOwner(), project.getNamespace().getSlug()));
        } else {
            project.setIconUrl(StringUtils.avatarUrl(project.getNamespace().getOwner()));
        }
    }
}

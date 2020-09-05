package io.papermc.hangar.service.api;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.api.ProjectsApiDao;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.generated.Project;
import io.papermc.hangar.model.generated.ProjectMember;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import io.papermc.hangar.model.generated.ProjectStatsDay;
import io.papermc.hangar.model.generated.Tag;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
import io.papermc.hangar.util.ApiUtil;
import io.papermc.hangar.util.Routes;
import io.papermc.hangar.util.TemplateHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectApiService {

    private final HangarConfig hangarConfig;
    private final HangarDao<ProjectsApiDao> projectApiDao;
    private final ProjectFiles projectFiles;
    private final TemplateHelper templateHelper;

    public ProjectApiService(HangarConfig hangarConfig, HangarDao<ProjectsApiDao> projectApiDao, ProjectFiles projectFiles, TemplateHelper templateHelper) {
        this.hangarConfig = hangarConfig;
        this.projectApiDao = projectApiDao;
        this.projectFiles = projectFiles;
        this.templateHelper = templateHelper;
    }

    public Project getProject(String pluginId, boolean seeHidden, Long requesterId) {
        Project project = projectApiDao.get().listProjects(pluginId, null, seeHidden, requesterId, null, null, null, null, null, 1, 0).stream().findFirst().orElse(null);
        if (project != null) {
            setProjectIconUrl(project);
        }
        return project;
    }

    public List<Project> getProjects(String pluginId, List<Category> categories, List<Tag> tags, String query, String owner, boolean seeHidden, Long requesterId, ProjectSortingStrategy sort, boolean orderWithRelevance, long limit, long offset) {
        String ordering = ApiUtil.strategyOrDefault(sort).getSql();
        if (orderWithRelevance && query != null && !query.isEmpty()) {
            String relevance = "ts_rank(p.search_words, websearch_to_tsquery_postfix('english', :query)) DESC";
            if(query.endsWith(" ")) {
                relevance = "ts_rank(p.search_words, websearch_to_tsquery('english', :query)) DESC";
            }
            String orderingFirstHalf;
            // 1483056000 is the Ore epoch
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
        List<Project> projects = projectApiDao.get().listProjects(pluginId, owner, seeHidden, requesterId, ordering, getCategoryNumbers(categories), getTagsNames(tags), trimQuery(query), getQueryStatement(query), limit, offset);
        projects.forEach(this::setProjectIconUrl);
        return projects;
    }

    private void setProjectIconUrl(Project project) {
        Path iconPath = projectFiles.getIconPath(project.getNamespace().getOwner(), project.getNamespace().getSlug());
        if (iconPath != null) {
            project.setIconUrl(hangarConfig.getBaseUrl() + Routes.getRouteUrlOf("projects.showIcon", project.getNamespace().getOwner(), project.getNamespace().getSlug()));
        } else {
            project.setIconUrl(templateHelper.avatarUrl(project.getNamespace().getOwner()));
        }
    }

    public long countProjects(String pluginId, List<Category> categories, List<Tag> tags, String query, String owner, boolean seeHidden, Long requesterId) {
        return projectApiDao.get().countProjects(pluginId, owner, seeHidden, requesterId, getCategoryNumbers(categories), getTagsNames(tags), trimQuery(query), getQueryStatement(query));
    }

    public List<ProjectMember> getProjectMembers(String pluginId, long limit, long offset) {
        return projectApiDao.get().projectMembers(pluginId, limit, offset);
    }

    public Map<String, ProjectStatsDay> getProjectStats(String pluginId, LocalDate fromDate, LocalDate toDate) {
        return projectApiDao.get().projectStats(pluginId, fromDate, toDate);
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

    private List<Integer> getCategoryNumbers(List<Category> categories){
        return categories == null ? null : categories.stream().map(Category::getValue).collect(Collectors.toList());
    }

    private List<String> getTagsNames(List<Tag> tags){
        return tags == null ? null : tags.stream().filter(tag -> tag.getData() == null).map(Tag::getName).collect(Collectors.toList());
    }

    private List<String> getTagsNamesAndVersion(List<Tag> tags){
        return tags == null ? null :  tags.stream().filter(tag -> tag.getData() != null).map(tag -> " (" + tag.getName() + "," +  tag.getData() + ") ").collect(Collectors.toList());
    }
}

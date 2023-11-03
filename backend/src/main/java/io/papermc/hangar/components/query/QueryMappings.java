package io.papermc.hangar.components.query;

import graphql.schema.DataFetchingEnvironment;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.service.internal.file.FileService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import static io.papermc.hangar.components.query.QueryBuilder.getActiveQueryBuilder;
import static io.papermc.hangar.components.query.QueryHelper.EMPTY;
import static io.papermc.hangar.components.query.QueryHelper.avatarUrl;
import static io.papermc.hangar.components.query.QueryHelper.join;
import static io.papermc.hangar.components.query.QueryHelper.query;
import static io.papermc.hangar.components.query.QueryHelper.selectField;

@Controller
public class QueryMappings {

    private final FileService fileService;
    private final AvatarService avatarService;

    public QueryMappings(final FileService fileService, final AvatarService avatarService) {
        this.fileService = fileService;
        this.avatarService = avatarService;
    }

    // queries
    @QueryMapping
    public Object projectBySlug(final DataFetchingEnvironment environment) {
        final String segmentName = environment.getExecutionStepInfo().getPath().getSegmentName();
        return query(environment, "projects", STR."WHERE \{segmentName}.slug = :slug");
    }

    @QueryMapping
    public Object projects(final DataFetchingEnvironment environment) {
        return query(environment, "projects");
    }

    @QueryMapping
    public Object users(final DataFetchingEnvironment environment) {
        return query(environment, "users");
    }

    // joins
    @SchemaMapping(typeName = "User", field = "projects")
    public Object userProjects(final DataFetchingEnvironment environment) {
        return join(environment, "projects", "projects", "owner_id", "id");
    }

    @SchemaMapping(typeName = "Project", field = "owner")
    public Object projectOwner(final DataFetchingEnvironment environment) {
        return join(environment, "users", "owner", "id", "owner_id");
    }

    @SchemaMapping(typeName = "Project", field = "pages")
    public Object projectPages(final DataFetchingEnvironment environment) {
        return join(environment, "project_pages", "pages", "project_id", "id");
    }

    // special schemas
    @SchemaMapping(typeName = "Project", field = "avatarUrl")
    public Object projectAvatarUrl(final DataFetchingEnvironment environment) {
        return avatarUrl(environment, this.fileService, this.avatarService, AvatarService.PROJECT);
    }

    @SchemaMapping(typeName = "User", field = "avatarUrl")
    public Object userUrl(final DataFetchingEnvironment environment) {
        return avatarUrl(environment, this.fileService, this.avatarService, AvatarService.USER);
    }

    @SchemaMapping(typeName = "Project", field = "namespace")
    public Object projectNamespace(final DataFetchingEnvironment environment) {
        selectField(environment, "", "owner", "owner_name", "namespace_owner");
        selectField(environment, "", "slug", "slug", "namespace_slug");
        return null; // no need to dig deeper
    }

    @SchemaMapping(typeName = "Project", field = "homepage")
    public Object projectHomepage(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = getActiveQueryBuilder(environment.getGraphQlContext());
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo().getParent(), queryBuilder);
        join(environment, "project_home_pages", "homepage_id", "project_id", "id");
        join(environment, "project_pages", "homepage", "id", "page_id", parentAlias + "homepage_id.");
        return EMPTY;
    }

    @SchemaMapping(typeName = "Project", field = "stats")
    public Object projectStats(final DataFetchingEnvironment environment) {
        join(environment, "home_projects", "extra", "id", "id");
        selectField(environment, "_extra", "stars", "stars", "stats_stars");
        selectField(environment, "_extra", "watchers", "watchers", "stats_watchers");
        selectField(environment, "_extra", "views", "views", "stats_views");
        selectField(environment, "_extra", "downloads", "downloads", "stats_downloads");
        selectField(environment, "_extra", "recentViews", "recent_views", "stats_recentViews");
        selectField(environment, "_extra", "recentDownloads", "recent_downloads", "stats_recentDownloads");
        return null; // no need to dig deeper
    }
}

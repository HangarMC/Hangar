package io.papermc.hangar.components.query;

import graphql.schema.DataFetchingEnvironment;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.service.internal.file.FileService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import static io.papermc.hangar.components.query.QueryHelper.avatarUrl;
import static io.papermc.hangar.components.query.QueryHelper.join;
import static io.papermc.hangar.components.query.QueryHelper.query;

@Controller
public class QueryMappings {

    private final FileService fileService;

    public QueryMappings(final FileService fileService) {
        this.fileService = fileService;
    }

    // queries
    @QueryMapping
    public Object projectBySlug(final DataFetchingEnvironment environment) {
        return query(environment, "projects", "WHERE projects.slug = :slug");
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

    // special schemas
    @SchemaMapping(typeName = "Project", field = "avatarUrl")
    public Object projectAvatarUrl(final DataFetchingEnvironment environment) {
        return avatarUrl(environment, this.fileService, AvatarService.PROJECT);
    }

    @SchemaMapping(typeName = "User", field = "avatarUrl")
    public Object userUrl(final DataFetchingEnvironment environment) {
        return avatarUrl(environment, this.fileService, AvatarService.USER);
    }

    @SchemaMapping(typeName = "Project", field = "namespace")
    public Object projectNamespace(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        final String parentTable = PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        if (environment.getSelectionSet().contains("owner")) {
            queryBuilder.fields.add(STR."\{parentTable}owner_name AS \{parentAlias}namespace_owner");
        }
        if (environment.getSelectionSet().contains("slug")) {
            queryBuilder.fields.add(STR."\{parentTable}slug AS \{parentAlias}namespace_slug");
        }
        return null; // no need to dig deeper
    }
}

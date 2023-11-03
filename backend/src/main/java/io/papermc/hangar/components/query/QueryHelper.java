package io.papermc.hangar.components.query;

import graphql.schema.DataFetchingEnvironment;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.service.internal.file.FileService;
import java.util.List;

import static io.papermc.hangar.components.query.QueryBuilder.getActiveQueryBuilder;
import static io.papermc.hangar.components.query.QueryBuilder.newQueryBuilder;

public final class QueryHelper {

    public static final Object EMPTY = new Object();
    public static final List<Object> EMPTY_LIST = List.of(EMPTY);

    private QueryHelper() {
    }

    public static List<Object> query(final DataFetchingEnvironment environment, final String rootTable) {
        return query(environment, rootTable, "");
    }

    public static List<Object> query(final DataFetchingEnvironment environment, final String rootTable, final String condition) {
        final QueryBuilder queryBuilder = newQueryBuilder(environment.getGraphQlContext());
        queryBuilder.variables = environment.getExecutionStepInfo().getArguments();
        queryBuilder.rootTable = environment.getExecutionStepInfo().getPath().getSegmentName();
        queryBuilder.from = STR."FROM \{rootTable} \{queryBuilder.rootTable}";
        queryBuilder.condition = condition;
        return EMPTY_LIST;
    }
    public static List<Object> join(final DataFetchingEnvironment environment, final String table, final String alias, final String fieldA, final String fieldB) {
        return join(environment, table, alias, fieldA, fieldB, null);
    }

    public static List<Object> join(final DataFetchingEnvironment environment, final String table, final String alias, final String fieldA, final String fieldB, final String secondTable) {
        final QueryBuilder queryBuilder = getActiveQueryBuilder(environment.getGraphQlContext());
        final String parentTable = secondTable == null ? PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder) : secondTable;
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        queryBuilder.joins.add(STR."LEFT JOIN \{table} \{parentAlias}\{alias} ON \{parentAlias}\{alias}.\{fieldA} = \{parentTable}\{fieldB}");
        return EMPTY_LIST;
    }

    public static void selectField(final DataFetchingEnvironment environment, final String tableSuffix, final String qglField, final String dbField, final String resultField) {
        if (environment.getSelectionSet().contains(qglField)) {
            final QueryBuilder queryBuilder = getActiveQueryBuilder(environment.getGraphQlContext());
            final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
            queryBuilder.fields.add(STR."\{parentAlias.substring(0, parentAlias.length() - 1)}\{tableSuffix}.\{dbField} AS \{parentAlias}\{resultField}");
        }
    }

    public static Object avatarUrl(final DataFetchingEnvironment environment, final FileService fileService,  final AvatarService avatarService, final String avatarType) {
        final String idVar = avatarType.equals(AvatarService.USER) ? "userid" : "projectid";
        final String idField = avatarType.equals(AvatarService.USER) ? "uuid" : "id";

        final QueryBuilder queryBuilder = getActiveQueryBuilder(environment.getGraphQlContext());
        final String parentTable = PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);

        final String avatarVersion = STR."ext_\{parentAlias.replace("_", "")}avatarversion";
        final String id = STR."ext_\{parentAlias.replace("_", "")}\{idVar}";
        queryBuilder.fields.add(STR."\{parentAlias}avatar.version AS \{avatarVersion}");
        queryBuilder.fields.add(STR."\{parentTable}\{idField} AS \{id}");

        queryBuilder.joins.add(STR."LEFT JOIN avatars \{parentAlias}avatar ON \{parentAlias}avatar.type = '\{avatarType}' AND \{parentAlias}avatar.subject = \{parentTable}\{idField}::varchar");
        queryBuilder.resolver.put(parentAlias + environment.getExecutionStepInfo().getPath().getSegmentName(), (r) -> {
            // TODO for projects we need to call up to the owner and get the avatar from there? or should we handle that in frontend?
            if (r.get(avatarVersion) == null) {
                return avatarService.getDefaultAvatarUrl();
            }
            return fileService.getAvatarUrl(avatarType, String.valueOf(r.get(id)), r.get(avatarVersion));
        });
        return EMPTY;
    }
}

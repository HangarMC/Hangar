package io.papermc.hangar.components.query;

import graphql.schema.DataFetchingEnvironment;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.service.internal.file.FileService;
import java.util.List;

public final class QueryHelper {

    public static final Object EMPTY = new Object();
    public static final List<Object> EMPTY_LIST = List.of(EMPTY);

    private QueryHelper() {
    }

    public static List<Object> query(final DataFetchingEnvironment environment, final String rootTable, final String rootAlias) {
        return query(environment, rootTable, rootAlias, "");
    }

    public static List<Object> query(final DataFetchingEnvironment environment, final String rootTable, final String rootAlias, final String condition) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        queryBuilder.rootTable = rootAlias;
        queryBuilder.from = STR."FROM \{rootTable} \{rootAlias}";
        queryBuilder.condition = condition;
        return EMPTY_LIST;
    }
    public static List<Object> join(final DataFetchingEnvironment environment, final String table, final String alias, final String fieldA, final String fieldB) {
        return join(environment, table, alias, fieldA, fieldB, null);
    }

    public static List<Object> join(final DataFetchingEnvironment environment, final String table, final String alias, final String fieldA, final String fieldB, final String secondTable) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentTable = secondTable == null ? PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder) : secondTable;
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        queryBuilder.joins.add(STR."JOIN \{table} \{parentAlias}\{alias} ON \{parentAlias}\{alias}.\{fieldA} = \{parentTable}\{fieldB}");
        return EMPTY_LIST;
    }

    public static Object avatarUrl(final DataFetchingEnvironment environment, final FileService fileService, final String avatarType) {
        final String idVar = avatarType.equals(AvatarService.USER) ? "userid" : "projectid";
        final String idField = avatarType.equals(AvatarService.USER) ? "uuid" : "id";

        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentTable = PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);

        final String avatarVersion = STR."ext_\{parentAlias.replace("_", "")}avatarversion";
        final String id = STR."ext_\{parentAlias.replace("_", "")}\{idVar}";
        queryBuilder.fields.add(STR."\{parentAlias}avatar.version AS \{avatarVersion}");
        queryBuilder.fields.add(STR."\{parentTable}\{idField} AS \{id}");

        queryBuilder.joins.add(STR."JOIN avatars \{parentAlias}avatar ON \{parentAlias}avatar.type = '\{avatarType}' AND \{parentAlias}avatar.subject = \{parentTable}\{idField}::varchar");
        queryBuilder.resolver.put(parentAlias + "avatarUrl", (r) -> fileService.getAvatarUrl(avatarType, String.valueOf(r.get(id)), r.get(avatarVersion)));
        return EMPTY;
    }
}

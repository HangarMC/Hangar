package io.papermc.hangar.components.query;

import graphql.schema.DataFetchingEnvironment;
import java.util.List;

public final class QueryHelper {

    public static final Object EMPTY = new Object();
    public static final List<Object> EMPTY_LIST = List.of(EMPTY);

    private QueryHelper() {
    }

    public static List<Object> query(final DataFetchingEnvironment environment, final String projects) {
        return query(environment, projects, "");
    }

    public static List<Object> query(final DataFetchingEnvironment environment, final String projects, final String condition) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        queryBuilder.rootTable = projects;
        queryBuilder.from = STR."FROM \{queryBuilder.rootTable}";
        queryBuilder.condition = condition;
        return EMPTY_LIST;
    }

    public static List<Object> join(final DataFetchingEnvironment environment, String table, String alias, String fieldA, String fieldB) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentTable = PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        queryBuilder.joins.add(STR."JOIN \{table} \{parentAlias}\{alias} ON \{parentAlias}\{alias}.\{fieldA} = \{parentTable}\{fieldB}");
        return EMPTY_LIST;
    }
}

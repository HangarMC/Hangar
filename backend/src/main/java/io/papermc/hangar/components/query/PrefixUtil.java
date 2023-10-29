package io.papermc.hangar.components.query;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLList;

public final class PrefixUtil {

    private PrefixUtil() {
    }

    public static String getParentAlias(final ExecutionStepInfo info, final QueryBuilder queryBuilder) {
        return getParentTable(info, queryBuilder, true);
    }

    public static String getParentTable(final ExecutionStepInfo info, final QueryBuilder queryBuilder) {
        return getParentTable(info, queryBuilder, false);
    }

    private static String getParentTable(final ExecutionStepInfo info, final QueryBuilder queryBuilder, final boolean alias) {
        final ExecutionStepInfo parent = info.getParent();
        if (parent == null || parent.getObjectType() == null || parent.getObjectType().getName().equals("Query")) {
            return queryBuilder.rootTable + (alias ? "_" : ".");
        } else if (parent.getType() instanceof GraphQLList) {
            // skip lists, else we would match them twice
            return getParentTable(parent, queryBuilder, true);
        } else {
            return getParentTable(parent, queryBuilder, true) + parent.getField().getName() + (alias ? "_" : ".");
        }
    }
}

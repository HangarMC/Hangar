package io.papermc.hangar.controller.extras.pagination;

import org.jdbi.v3.core.statement.SqlStatement;

public interface Filter {

    void createSql(StringBuilder sb, SqlStatement<?> q);
}

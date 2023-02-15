package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public record LogProject(@PropagateNull Long id, String slug, @ColumnName("owner_name") String owner) {
}

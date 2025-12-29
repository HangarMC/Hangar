package io.papermc.hangar.components.health.model;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

public record UnhealthyProject(@Nested ProjectNamespace namespace, OffsetDateTime lastUpdated, @EnumByOrdinal Visibility visibility) {

}

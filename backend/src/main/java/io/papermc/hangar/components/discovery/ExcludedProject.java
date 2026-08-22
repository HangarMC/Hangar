package io.papermc.hangar.components.discovery;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public record ExcludedProject(
    long projectId,
    String name,
    @Nested ProjectNamespace namespace,
    OffsetDateTime createdAt,
    @Nullable String excludedBy
) {

    @JdbiConstructor
    public ExcludedProject {
    }
}

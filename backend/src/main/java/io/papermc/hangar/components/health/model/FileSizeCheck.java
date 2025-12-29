package io.papermc.hangar.components.health.model;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import org.jdbi.v3.core.mapper.Nested;

public record FileSizeCheck(@Nested ProjectNamespace namespace, long totalSize, long fileCount) {
}

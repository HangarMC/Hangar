package io.papermc.hangar.model.internal.admin;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import org.jdbi.v3.core.mapper.Nested;

public record TopProject(@Nested ProjectNamespace namespace, long downloads, long views) {
}

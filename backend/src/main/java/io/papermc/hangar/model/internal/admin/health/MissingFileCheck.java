package io.papermc.hangar.model.internal.admin.health;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.mapper.Nested;

public record MissingFileCheck(Platform platform, String versionString, String fileName, @Nested ProjectNamespace namespace, String name) {
}

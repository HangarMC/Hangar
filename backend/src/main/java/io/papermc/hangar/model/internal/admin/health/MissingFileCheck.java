package io.papermc.hangar.model.internal.admin.health;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Platform;
import java.util.List;
import org.jdbi.v3.core.mapper.Nested;

public record MissingFileCheck(@Nested ProjectNamespace namespace, String versionString, List<Platform> platforms, List<String> fileNames) {
}

package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.ReviewState;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public record VersionToScan(long versionId,
                     long projectId,
                     @EnumByOrdinal ReviewState reviewState,
                     String versionString,
                     List<Platform> platforms) {
}

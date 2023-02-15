package io.papermc.hangar.model.internal.logs.viewmodels;

import io.papermc.hangar.model.common.Platform;
import java.util.List;
import org.jdbi.v3.core.mapper.PropagateNull;

public record LogVersion(@PropagateNull Long id, String versionString, List<Platform> platforms) {
}

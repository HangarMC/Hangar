package io.papermc.hangar.model.internal.logs.viewmodels;

import io.papermc.hangar.model.common.Platform;
import java.util.List;
import org.jdbi.v3.core.mapper.PropagateNull;

// jdbi doesnt like the platforms when the list is empty :shrug:
public record LogVersion(@PropagateNull Long id, String versionString /*, List<Platform> platforms */) {
}

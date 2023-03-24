package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.Named;
import org.jdbi.v3.core.mapper.PropagateNull;

public record FileInfo(@PropagateNull String name, long sizeBytes, String sha256Hash) implements Named {

    @Override
    public String getName() {
        return this.name;
    }
}

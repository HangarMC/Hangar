package io.papermc.hangar.model.api.project.version;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.jdbi.v3.core.enums.EnumByName;

@EnumByName
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum PinnedStatus {
    NONE,
    VERSION,
    CHANNEL
}

package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.jdbi.v3.core.enums.EnumByOrdinal;

@EnumByOrdinal
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ChannelFlag {
    FROZEN(false),
    UNSTABLE(true),
    SKIP_REVIEW_QUEUE(true);

    private final boolean editable;

    ChannelFlag(final boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return this.editable;
    }
}

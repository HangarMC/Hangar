package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.jdbi.v3.core.enums.EnumByOrdinal;

@EnumByOrdinal
@JsonFormat(shape = JsonFormat.Shape.STRING)
// remember never to delete or re-order these, just deprecate it
public enum ChannelFlag {
    FROZEN(false),
    UNSTABLE(true),
    SKIP_REVIEW_QUEUE(true),
    PINNED(true),
    ;

    private final boolean editable;

    ChannelFlag(final boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return this.editable;
    }
}

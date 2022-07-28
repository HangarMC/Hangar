package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.jdbi.v3.core.enums.EnumByOrdinal;

@EnumByOrdinal
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ChannelFlag {
    FROZEN(false, false),
    UNSTABLE(true, false),
    PINNED(true, true),
    ;

    private final boolean editable;
    private final boolean alwaysEditable;

    ChannelFlag(final boolean editable, final boolean alwaysEditable) {
        this.editable = editable;
        this.alwaysEditable = alwaysEditable;
    }

    public boolean isEditable() {
        return this.editable;
    }

    /**
     * Returns whether the flag is editable even if the channel is frozen.
     *
     * @return whether the flag is editable even if the channel is frozen
     */
    public boolean isAlwaysEditable() {
        return alwaysEditable;
    }
}

package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.jdbi.v3.core.enums.EnumByOrdinal;

@EnumByOrdinal
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ChannelFlag {
    FROZEN(false, false),
    UNSTABLE(true, false),
    PINNED(true, true),
    SENDS_NOTIFICATIONS(true, false),
    ;

    public static final Set<ChannelFlag> EDITABLE = Arrays.stream(values()).filter(ChannelFlag::isEditable).collect(Collectors.toUnmodifiableSet());
    public static final Set<ChannelFlag> ALWAYS_EDITABLE = Arrays.stream(values()).filter(ChannelFlag::isAlwaysEditable).collect(Collectors.toUnmodifiableSet());

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
        return this.alwaysEditable;
    }
}

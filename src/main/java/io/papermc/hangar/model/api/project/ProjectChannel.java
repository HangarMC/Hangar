package io.papermc.hangar.model.api.project;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public class ProjectChannel extends Model implements Named {

    private final String name;
    private final Color color;
    private final Set<ChannelFlag> flags;

    public ProjectChannel(final OffsetDateTime createdAt, final String name, @EnumByOrdinal final Color color, final Set<ChannelFlag> flags) {
        super(createdAt);
        this.name = name;
        this.color = color;
        this.flags = flags;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Color getColor() {
        return this.color;
    }

    public Set<ChannelFlag> getFlags() {
        return Collections.unmodifiableSet(this.flags);
    }

    @Override
    public String toString() {
        return "ProjectChannel{" +
            "flags=" + this.flags +
            ", name='" + this.name + '\'' +
            ", color='" + this.color + '\'' +
            "} " + super.toString();
    }
}

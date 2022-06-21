package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;
import java.util.Set;

public class HangarChannel extends ProjectChannelTable {

    private final int versionCount;

    public HangarChannel(final OffsetDateTime createdAt, final long id, final String name, @EnumByOrdinal final Color color, final long projectId, final Set<ChannelFlag> flags, final int versionCount) {
        super(createdAt, id, name, color, projectId, flags);
        this.versionCount = versionCount;
    }

    public int getVersionCount() {
        return this.versionCount;
    }

    @Override
    public String toString() {
        return "HangarChannel{" +
                "projectCount=" + this.versionCount +
                "} " + super.toString();
    }
}

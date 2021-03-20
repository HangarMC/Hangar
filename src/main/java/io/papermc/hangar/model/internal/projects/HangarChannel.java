package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public class HangarChannel extends ProjectChannelTable {

    private final int versionCount;

    public HangarChannel(OffsetDateTime createdAt, long id, String name, @EnumByOrdinal Color color, long projectId, boolean nonReviewed, int versionCount) {
        super(createdAt, id, name, color, projectId, nonReviewed);
        this.versionCount = versionCount;
    }

    public int getVersionCount() {
        return versionCount;
    }

    @Override
    public String toString() {
        return "HangarChannel{" +
                "projectCount=" + versionCount +
                "} " + super.toString();
    }
}

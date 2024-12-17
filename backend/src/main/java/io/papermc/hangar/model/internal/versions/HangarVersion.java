package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.config.jackson.RequiresPermission;
import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.api.project.ProjectChannel;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class HangarVersion extends Version implements Identified {

    private final String approvedBy;

    public HangarVersion(final OffsetDateTime createdAt, final long id, @ColumnName("version_string") final String name, final Visibility visibility, final String description, @Nested("vs") final VersionStats stats, final String author, @EnumByOrdinal final ReviewState reviewState, @Nested("pc") final ProjectChannel channel, final PinnedStatus pinnedStatus, final String approvedBy) {
        super(createdAt, id, name, visibility, description, stats, author, reviewState, channel, pinnedStatus);
        this.approvedBy = approvedBy;
    }

    @RequiresPermission(NamedPermission.REVIEWER)
    public String getApprovedBy() {
        return this.approvedBy;
    }

    @Override
    public String toString() {
        return "HangarVersion{" +
            ", approvedBy='" + this.approvedBy + '\'' +
            "} " + super.toString();
    }
}

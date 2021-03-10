package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.config.jackson.RequiresPermission;
import io.papermc.hangar.model.Identified;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.OffsetDateTime;

public class HangarVersion extends Version implements Identified {

    private final long id;
    private final String approvedBy;

    public HangarVersion(OffsetDateTime createdAt, @ColumnName("version_string") String name, Visibility visibility, String description, @Nested("vs") VersionStats stats, @Nested("fi") FileInfo fileInfo, String externalUrl, String author, @EnumByOrdinal ReviewState reviewState, boolean recommended, long id, String approvedBy) {
        super(createdAt, name, visibility, description, stats, fileInfo, externalUrl, author, reviewState, recommended);
        this.id = id;
        this.approvedBy = approvedBy;
    }

    @Override
    public long getId() {
        return id;
    }

    @RequiresPermission(NamedPermission.REVIEWER)
    public String getApprovedBy() {
        return approvedBy;
    }

    @Override
    public String toString() {
        return "HangarVersion{" +
                "id=" + id +
                ", approvedBy='" + approvedBy + '\'' +
                "} " + super.toString();
    }
}

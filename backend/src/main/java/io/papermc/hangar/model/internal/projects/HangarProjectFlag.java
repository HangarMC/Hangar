package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jetbrains.annotations.Nullable;

public class HangarProjectFlag extends ProjectFlagTable {

    private final String reportedByName;
    private final String resolvedByName;
    private final ProjectNamespace projectNamespace;
    private final Visibility projectVisibility;

    public HangarProjectFlag(final OffsetDateTime createdAt, final long id, final long projectId, final long userId, @EnumByOrdinal final FlagReason reason, final boolean resolved, final String comment, final OffsetDateTime resolvedAt, final Long resolvedBy, final String reportedByName, final @Nullable String resolvedByName, final String projectOwnerName, final String projectSlug, @EnumByOrdinal final Visibility projectVisibility) {
        super(createdAt, id, projectId, userId, reason, resolved, comment, resolvedAt, resolvedBy);
        this.reportedByName = reportedByName;
        this.resolvedByName = resolvedByName;
        this.projectNamespace = new ProjectNamespace(projectOwnerName, projectSlug);
        this.projectVisibility = projectVisibility;
    }

    public String getReportedByName() {
        return this.reportedByName;
    }

    public String getResolvedByName() {
        return this.resolvedByName;
    }

    public ProjectNamespace getProjectNamespace() {
        return this.projectNamespace;
    }

    public Visibility getProjectVisibility() {
        return this.projectVisibility;
    }

    @Override
    public String toString() {
        return "HangarProjectFlag{" +
            "reportedByName='" + this.reportedByName + '\'' +
            ", resolvedByName='" + this.resolvedByName + '\'' +
            ", projectNamespace=" + this.projectNamespace +
            ", projectVisibility=" + this.projectVisibility +
            "} " + super.toString();
    }
}

package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public class HangarProjectFlag extends ProjectFlagTable {

    private final String reportedByName;
    private final String resolvedByName;
    private final ProjectNamespace projectNamespace;
    private final Visibility projectVisibility;

    public HangarProjectFlag(OffsetDateTime createdAt, long id, long projectId, long userId, @EnumByOrdinal FlagReason reason, boolean resolved, String comment, OffsetDateTime resolvedAt, Long resolvedBy, String reportedByName, @Nullable String resolvedByName, String projectOwnerName, String projectSlug, @EnumByOrdinal Visibility projectVisibility) {
        super(createdAt, id, projectId, userId, reason, resolved, comment, resolvedAt, resolvedBy);
        this.reportedByName = reportedByName;
        this.resolvedByName = resolvedByName;
        this.projectNamespace = new ProjectNamespace(projectOwnerName, projectSlug);
        this.projectVisibility = projectVisibility;
    }

    public String getReportedByName() {
        return reportedByName;
    }

    public String getResolvedByName() {
        return resolvedByName;
    }

    public ProjectNamespace getProjectNamespace() {
        return projectNamespace;
    }

    public Visibility getProjectVisibility() {
        return projectVisibility;
    }

    @Override
    public String toString() {
        return "HangarProjectFlag{" +
                "reportedByName='" + reportedByName + '\'' +
                ", resolvedByName='" + resolvedByName + '\'' +
                ", projectNamespace=" + projectNamespace +
                ", projectVisibility=" + projectVisibility +
                "} " + super.toString();
    }
}

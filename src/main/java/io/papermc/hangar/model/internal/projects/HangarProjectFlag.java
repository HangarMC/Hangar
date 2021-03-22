package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public class HangarProjectFlag extends ProjectFlagTable {

    private final String reportedByName;
    private final String resolvedByName;
    private final String projectOwnerName;
    private final String projectSlug;
    private final Visibility projectVisibility;

    public HangarProjectFlag(OffsetDateTime createdAt, long id, long projectId, long userId, @EnumByOrdinal FlagReason reason, boolean resolved, String comment, OffsetDateTime resolvedAt, long resolvedBy, String reportedByName, String resolvedByName, String projectOwnerName, String projectSlug, @EnumByOrdinal Visibility projectVisibility) {
        super(createdAt, id, projectId, userId, reason, resolved, comment, resolvedAt, resolvedBy);
        this.reportedByName = reportedByName;
        this.resolvedByName = resolvedByName;
        this.projectOwnerName = projectOwnerName;
        this.projectSlug = projectSlug;
        this.projectVisibility = projectVisibility;
    }

    public String getReportedByName() {
        return reportedByName;
    }

    public String getResolvedByName() {
        return resolvedByName;
    }

    public String getProjectOwnerName() {
        return projectOwnerName;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public Visibility getProjectVisibility() {
        return projectVisibility;
    }

    public String getProjectNamespace() {
        return projectOwnerName + "/" + projectSlug;
    }

    @Override
    public String toString() {
        return "HangarProjectFlag{" +
                "reportedByName='" + reportedByName + '\'' +
                ", resolvedByName='" + resolvedByName + '\'' +
                ", projectOwnerName='" + projectOwnerName + '\'' +
                ", projectSlug='" + projectSlug + '\'' +
                ", projectVisibility=" + projectVisibility +
                "} " + super.toString();
    }
}

package io.papermc.hangar.model.internal.projects;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

public class HangarProjectApproval {

    private final long projectId;
    private final ProjectNamespace namespace;
    private final Visibility visibility;
    private final String comment;
    private final String changeRequester;

    public HangarProjectApproval(long projectId, @Nested("pn") ProjectNamespace namespace, @EnumByOrdinal Visibility visibility, String comment, String changeRequester) {
        this.projectId = projectId;
        this.namespace = namespace;
        this.visibility = visibility;
        this.comment = comment;
        this.changeRequester = changeRequester;
    }

    public long getProjectId() {
        return projectId;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public String getComment() {
        return comment;
    }

    public String getChangeRequester() {
        return changeRequester;
    }

    @Override
    public String toString() {
        return "HangarProjectApproval{" +
                "projectId=" + projectId +
                ", namespace=" + namespace +
                ", visibility=" + visibility +
                ", comment='" + comment + '\'' +
                ", changeRequester='" + changeRequester + '\'' +
                '}';
    }
}

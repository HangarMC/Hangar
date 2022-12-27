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

    public HangarProjectApproval(final long projectId, @Nested("pn") final ProjectNamespace namespace, @EnumByOrdinal final Visibility visibility, final String comment, final String changeRequester) {
        this.projectId = projectId;
        this.namespace = namespace;
        this.visibility = visibility;
        this.comment = comment;
        this.changeRequester = changeRequester;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public ProjectNamespace getNamespace() {
        return this.namespace;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public String getComment() {
        return this.comment;
    }

    public String getChangeRequester() {
        return this.changeRequester;
    }

    @Override
    public String toString() {
        return "HangarProjectApproval{" +
            "projectId=" + this.projectId +
            ", namespace=" + this.namespace +
            ", visibility=" + this.visibility +
            ", comment='" + this.comment + '\'' +
            ", changeRequester='" + this.changeRequester + '\'' +
            '}';
    }
}

package io.papermc.hangar.model.db.versions.dependencies;

import io.papermc.hangar.model.common.Platform;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionDependencyTable extends VersionDependencyTable {

    private final Platform platform;
    private final String name;
    private boolean required;
    private Long projectId;
    private String externalUrl;

    @JdbiConstructor
    public ProjectVersionDependencyTable(final OffsetDateTime createdAt, final long id, final long versionId, @EnumByOrdinal final Platform platform, final String name, final boolean required, final Long projectId, final String externalUrl) {
        super(createdAt, id, versionId);
        this.platform = platform;
        this.name = name;
        this.required = required;
        this.projectId = projectId;
        this.externalUrl = externalUrl;
    }

    public ProjectVersionDependencyTable(final long versionId, final Platform platform, final String name, final boolean required, final Long projectId, final String externalUrl) {
        super(versionId);
        this.platform = platform;
        this.name = name;
        this.required = required;
        this.projectId = projectId;
        this.externalUrl = externalUrl;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return this.platform;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(final Long projectId) {
        this.projectId = projectId;
    }

    public String getExternalUrl() {
        return this.externalUrl;
    }

    public void setExternalUrl(final String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String toLogString() {
        return "Name: " + this.name + (this.required ? " (required)" : "");
    }

    @Override
    public String toString() {
        return "ProjectVersionDependencyTable{" +
            "platform=" + this.platform +
            ", name='" + this.name + '\'' +
            ", required=" + this.required +
            ", projectId=" + this.projectId +
            ", externalUrl='" + this.externalUrl + '\'' +
            "} " + super.toString();
    }
}

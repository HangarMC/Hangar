package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectVersionDependencyTable extends VersionDependencyTable {

    private final Platform platform;
    private final String name;
    private boolean required;
    private Long projectId;
    private String externalUrl;

    @JdbiConstructor
    public ProjectVersionDependencyTable(OffsetDateTime createdAt, long id, long versionId, @EnumByOrdinal Platform platform, String name, boolean required, Long projectId, String externalUrl) {
        super(createdAt, id, versionId);
        this.platform = platform;
        this.name = name;
        this.required = required;
        this.projectId = projectId;
        this.externalUrl = externalUrl;
    }

    public ProjectVersionDependencyTable(long versionId, Platform platform, String name, boolean required, Long projectId, String externalUrl) {
        super(versionId);
        this.platform = platform;
        this.name = name;
        this.required = required;
        this.projectId = projectId;
        this.externalUrl = externalUrl;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return platform;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String toLogString() {
        return "Name: " + name + (required ? " (required)" : "");
    }

    @Override
    public String toString() {
        return "ProjectVersionDependencyTable{" +
                "platform=" + platform +
                ", name='" + name + '\'' +
                ", required=" + required +
                ", projectId=" + projectId +
                ", externalUrl='" + externalUrl + '\'' +
                "} " + super.toString();
    }
}

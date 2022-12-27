package io.papermc.hangar.model.internal.admin.health;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.mapper.Nested;

public class MissingFileCheck {

    private final Platform platform;
    private final String versionString;
    private final String fileName;
    private final ProjectNamespace namespace;
    private final String name;

    public MissingFileCheck(final Platform platform, final String versionString, final String fileName, @Nested final ProjectNamespace namespace, final String name) {
        this.platform = platform;
        this.versionString = versionString;
        this.fileName = fileName;
        this.namespace = namespace;
        this.name = name;
    }

    public Platform getPlatform() {
        return this.platform;
    }

    public String getVersionString() {
        return this.versionString;
    }

    public String getFileName() {
        return this.fileName;
    }

    public ProjectNamespace getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "MissingFileCheck{" +
            "platform=" + this.platform +
            ", versionString='" + this.versionString + '\'' +
            ", fileName='" + this.fileName + '\'' +
            ", namespace=" + this.namespace +
            ", name='" + this.name + '\'' +
            '}';
    }
}

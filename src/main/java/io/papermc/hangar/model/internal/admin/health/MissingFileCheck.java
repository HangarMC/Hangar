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

    public MissingFileCheck(Platform platform, String versionString, String fileName, @Nested ProjectNamespace namespace, String name) {
        this.platform = platform;
        this.versionString = versionString;
        this.fileName = fileName;
        this.namespace = namespace;
        this.name = name;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getVersionString() {
        return versionString;
    }

    public String getFileName() {
        return fileName;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "MissingFileCheck{" +
                "platform=" + platform +
                ", versionString='" + versionString + '\'' +
                ", fileName='" + fileName + '\'' +
                ", namespace=" + namespace +
                ", name='" + name + '\'' +
                '}';
    }
}

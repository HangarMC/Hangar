package io.papermc.hangar.model.internal.admin.health;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonIgnore
    public Platform getPlatform() {
        return platform;
    }

    @JsonProperty("platform")
    public String _getPlatform() {
        return platform.getEnumName();
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

package io.papermc.hangar.model.internal.logs.viewmodels;

import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.mapper.PropagateNull;

import java.util.List;

public class LogVersion {

    private final Long id;
    private final String versionString;
    private final List<Platform> platforms;

    public LogVersion(@PropagateNull Long id, String versionString, List<Platform> platforms) {
        this.id = id;
        this.versionString = versionString;
        this.platforms = platforms;
    }

    public Long getId() {
        return id;
    }

    public String getVersionString() {
        return versionString;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    @Override
    public String toString() {
        return "LogVersion{" +
                "id=" + id +
                ", versionString='" + versionString + '\'' +
                ", platforms=" + platforms +
                '}';
    }
}

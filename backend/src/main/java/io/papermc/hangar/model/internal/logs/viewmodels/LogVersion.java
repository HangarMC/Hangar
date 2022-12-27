package io.papermc.hangar.model.internal.logs.viewmodels;

import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.mapper.PropagateNull;

import java.util.List;

public class LogVersion {

    private final Long id;
    private final String versionString;
    private final List<Platform> platforms;

    public LogVersion(@PropagateNull final Long id, final String versionString, final List<Platform> platforms) {
        this.id = id;
        this.versionString = versionString;
        this.platforms = platforms;
    }

    public Long getId() {
        return this.id;
    }

    public String getVersionString() {
        return this.versionString;
    }

    public List<Platform> getPlatforms() {
        return this.platforms;
    }

    @Override
    public String toString() {
        return "LogVersion{" +
                "id=" + this.id +
                ", versionString='" + this.versionString + '\'' +
                ", platforms=" + this.platforms +
                '}';
    }
}

package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;

public class LogVersion {

    private final Long id;
    // TODO maybe need the platform in here eventually?
    private final String versionString;

    public LogVersion(@PropagateNull Long id, String versionString) {
        this.id = id;
        this.versionString = versionString;
    }

    public Long getId() {
        return id;
    }

    public String getVersionString() {
        return versionString;
    }

    @Override
    public String toString() {
        return "LogProjectVersion{" +
                "id=" + id +
                ", versionString='" + versionString + '\'' +
                '}';
    }
}

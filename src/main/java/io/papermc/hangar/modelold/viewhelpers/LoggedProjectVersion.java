package io.papermc.hangar.modelold.viewhelpers;

import org.jetbrains.annotations.Nullable;

public class LoggedProjectVersion {

    private final Long id;
    private final String versionString;

    public LoggedProjectVersion(@Nullable Long id, @Nullable String versionString) {
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
        return "LoggedProjectVersion{" +
                "id=" + id +
                ", versionString='" + versionString + '\'' +
                '}';
    }
}

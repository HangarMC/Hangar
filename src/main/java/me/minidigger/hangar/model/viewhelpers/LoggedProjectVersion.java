package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.generated.Version;
import org.jetbrains.annotations.Nullable;

public class LoggedProjectVersion {

    private Version version;
    private String versionString;

    public LoggedProjectVersion(@Nullable Version version, @Nullable String versionString) {
        this.version = version;
        this.versionString = versionString;
    }

    @Nullable
    public Version getVersion() {
        return version;
    }

    @Nullable
    public String getVersionString() {
        return versionString;
    }
}

package io.papermc.hangar.model.internal.api.requests.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.Platform;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

public class UpdatePlatformVersions {

    @NotNull
    private final Platform platform;
    @Size(min = 1, message = "version.edit.error.noPlatformVersions")
    private final Set<@NotBlank(message = "version.new.error.invalidPlatformVersion") String> versions;

    @JsonCreator
    public UpdatePlatformVersions(Platform platform, Set<String> versions) {
        this.platform = platform;
        this.versions = versions;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Set<String> getVersions() {
        return versions;
    }

    @Override
    public String toString() {
        return "UpdatePlatformVersions{" +
                "platform=" + platform +
                ", versions=" + versions +
                '}';
    }
}

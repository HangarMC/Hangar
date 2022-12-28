package io.papermc.hangar.model.internal.api.requests.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class UpdatePlatformVersions {

    @NotNull
    private final Platform platform;
    @Size(min = 1, message = "version.edit.error.noPlatformVersions")
    private final Set<@NotBlank(message = "version.new.error.invalidPlatformVersion") String> versions;

    @JsonCreator
    public UpdatePlatformVersions(final Platform platform, final Set<String> versions) {
        this.platform = platform;
        this.versions = versions;
    }

    public Platform getPlatform() {
        return this.platform;
    }

    public Set<String> getVersions() {
        return this.versions;
    }

    @Override
    public String toString() {
        return "UpdatePlatformVersions{" +
            "platform=" + this.platform +
            ", versions=" + this.versions +
            '}';
    }
}

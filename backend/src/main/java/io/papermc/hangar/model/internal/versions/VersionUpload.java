package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.*;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import org.checkerframework.checker.nullness.qual.Nullable;

@Schema(description = "Model for a version upload")
public class VersionUpload {

    // @el(root: String)
    @NotBlank(message = "version.new.error.invalidVersionString")
    @Schema(description = "Version string of the version to be published", example = "1.0.0-SNAPSHOT+1")
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.projects.versionNameRegex)", message = "version.new.error.invalidVersionString") String version;
    @Schema(description = "Map of each platform's plugin dependencies")
    private final Map<Platform, Set<@Valid PluginDependency>> pluginDependencies;
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms")
    @Schema(description = "Map of platforms and their versions this version runs on", example = "{PAPER: [\"1.12\", \"1.16-1.18.2\", \"1.20.x\"]}")
    private final Map<Platform, @Size(min = 1, message = "version.edit.error.noPlatformVersions") SortedSet<@NotBlank(message = "version.new.error.invalidPlatformVersion") String>> platformDependencies;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxLen)", message = "page.new.error.maxLength") String description;
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms")
    private final List<@Valid MultipartFileOrUrl> files;

    // @el(root: String)
    @NotBlank(message = "version.new.error.channel.noName")
    @Schema(description = "Channel of the version to be published under", example = "Release")
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.channels.nameRegex)", message = "channel.modal.error.invalidName") String channel;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public VersionUpload(final String version, final Map<Platform, Set<PluginDependency>> pluginDependencies, final EnumMap<Platform, SortedSet<String>> platformDependencies, final @Nullable String description, final List<MultipartFileOrUrl> files, final String channel) {
        this.version = version;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        this.description = description != null ? description : "*No description provided*";
        this.files = files;
        this.channel = channel;
    }

    public String getVersion() {
        return this.version;
    }

    public Map<Platform, Set<PluginDependency>> getPluginDependencies() {
        return this.pluginDependencies;
    }

    public Map<Platform, SortedSet<String>> getPlatformDependencies() {
        return this.platformDependencies;
    }

    public String getDescription() {
        return this.description;
    }

    public List<MultipartFileOrUrl> getFiles() {
        return this.files;
    }

    public String getChannel() {
        return this.channel;
    }

    public PendingVersion toPendingVersion(final List<PendingVersionFile> files) {
        return new PendingVersion(this.version,
            this.pluginDependencies,
            (EnumMap<Platform, SortedSet<String>>) this.platformDependencies,
            this.description,
            files,
            this.channel,
            null,
            null,
            null
        );
    }

    @Override
    public String toString() {
        return "VersionUpload{" +
            "version='" + this.version + '\'' +
            ", pluginDependencies=" + this.pluginDependencies +
            ", platformDependencies=" + this.platformDependencies +
            ", description='" + this.description + '\'' +
            ", files=" + this.files +
            ", channel='" + this.channel + '\'' +
            '}';
    }
}

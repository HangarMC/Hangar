package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Platform;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.jetbrains.annotations.Nullable;

public class PendingVersion {

    // @el(root: String)
    @NotBlank(message = "version.new.error.invalidVersionString")
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.projects.versionNameRegex)", message = "version.new.error.invalidVersionString") String versionString;
    private final Map<Platform, Set<@Valid PluginDependency>> pluginDependencies;
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms")
    private final Map<Platform, @Size(min = 1, message = "version.edit.error.noPlatformVersions") SortedSet<@NotBlank(message = "version.new.error.invalidPlatformVersion") String>> platformDependencies;

    // @el(root: String)
    @NotBlank(message = "version.new.error.noDescription")
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxLen)", message = "page.new.error.maxLength") String description;
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms")
    private final List<@Valid PendingVersionFile> files;

    // @el(root: String)
    @NotBlank(message = "version.new.error.channel.noName")
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.channels.nameRegex)", message = "channel.modal.error.invalidName") String channelName;
    @NotNull(message = "version.new.error.channel.noColor")
    private final Color channelColor;
    private final Set<ChannelFlag> channelFlags;
    private final boolean forumSync;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PendingVersion(final String versionString, final Map<Platform, Set<PluginDependency>> pluginDependencies, final EnumMap<Platform, SortedSet<String>> platformDependencies, final String description, final List<PendingVersionFile> files, final String channelName, final Color channelColor, final Set<ChannelFlag> channelFlags, final boolean forumSync) {
        this.versionString = versionString;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        this.description = description;
        this.files = files;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.channelFlags = channelFlags;
        this.forumSync = forumSync;
    }

    public PendingVersion(final @Nullable String versionString, final Map<Platform, Set<PluginDependency>> pluginDependencies, final Map<Platform, SortedSet<String>> platformDependencies, final List<PendingVersionFile> files, final boolean forumSync) {
        this.versionString = versionString;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        this.description = null;
        this.forumSync = forumSync;
        this.files = files;
        // Keep data from frontend
        this.channelName = "";
        this.channelColor = Color.CYAN;
        this.channelFlags = Set.of();
    }

    public String getVersionString() {
        return this.versionString;
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

    public List<PendingVersionFile> getFiles() {
        return this.files;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public Color getChannelColor() {
        return this.channelColor;
    }

    public Set<ChannelFlag> getChannelFlags() {
        return this.channelFlags;
    }

    public boolean isForumSync() {
        return this.forumSync;
    }

    @Override
    public String toString() {
        return "PendingVersion{" +
            "versionString='" + this.versionString + '\'' +
            ", pluginDependencies=" + this.pluginDependencies +
            ", platformDependencies=" + this.platformDependencies +
            ", description='" + this.description + '\'' +
            ", files=" + this.files +
            ", channelName='" + this.channelName + '\'' +
            ", channelColor=" + this.channelColor +
            ", channelFlags=" + this.channelFlags +
            ", forumSync=" + this.forumSync +
            '}';
    }
}

package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Platform;
import org.jetbrains.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class PendingVersion {

    @NotBlank(message = "version.new.error.invalidVersionString")
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.projects.versionNameRegex)", message = "version.new.error.invalidVersionString")
    private final String versionString;
    private final Map<Platform, Set<@Valid PluginDependency>> pluginDependencies;
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms")
    private final Map<Platform, @Size(min = 1, message = "version.edit.error.noPlatformVersions") SortedSet<@NotBlank(message = "version.new.error.invalidPlatformVersion") String>> platformDependencies;
    @NotBlank(message = "version.new.error.noDescription")
    @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxLen)", message = "page.new.error.maxLength")
    private final String description;
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms")
    private final List<@Valid PendingVersionFile> files;
    @NotBlank(message = "version.new.error.channel.noName")
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.channels.nameRegex)", message = "channel.modal.error.invalidName")
    private final String channelName;
    @NotNull(message = "version.new.error.channel.noColor")
    private final Color channelColor;
    private final Set<ChannelFlag> channelFlags;
    private final boolean forumSync;

    @JsonCreator(mode = Mode.PROPERTIES)
    public PendingVersion(String versionString, Map<Platform, Set<PluginDependency>> pluginDependencies, EnumMap<Platform, SortedSet<String>> platformDependencies, String description, List<PendingVersionFile> files, String channelName, Color channelColor, Set<ChannelFlag> channelFlags, boolean forumSync) {
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

    public PendingVersion(@Nullable String versionString, Map<Platform, Set<PluginDependency>> pluginDependencies, Map<Platform, SortedSet<String>> platformDependencies, List<PendingVersionFile> files, boolean forumSync) {
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
        return versionString;
    }

    public Map<Platform, Set<PluginDependency>> getPluginDependencies() {
        return pluginDependencies;
    }

    public Map<Platform, SortedSet<String>> getPlatformDependencies() {
        return platformDependencies;
    }

    public String getDescription() {
        return description;
    }

    public List<PendingVersionFile> getFiles() {
        return files;
    }

    public String getChannelName() {
        return channelName;
    }

    public Color getChannelColor() {
        return channelColor;
    }

    public Set<ChannelFlag> getChannelFlags() {
        return channelFlags;
    }

    public boolean isForumSync() {
        return forumSync;
    }

    @Override
    public String toString() {
        return "PendingVersion{" +
            "versionString='" + versionString + '\'' +
            ", pluginDependencies=" + pluginDependencies +
            ", platformDependencies=" + platformDependencies +
            ", description='" + description + '\'' +
            ", files=" + files +
            ", channelName='" + channelName + '\'' +
            ", channelColor=" + channelColor +
            ", channelFlags=" + channelFlags +
            ", forumSync=" + forumSync +
            '}';
    }
}

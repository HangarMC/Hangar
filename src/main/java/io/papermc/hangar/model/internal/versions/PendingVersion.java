package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import org.jetbrains.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PendingVersion {

    @NotBlank(message = "version.new.error.invalidVersionString")
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.projects.versionNameRegex)", message = "version.new.error.invalidVersionString")
    private final String versionString;
    private final Map<Platform, Set<@Valid PluginDependency>> pluginDependencies;
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms")
    private final Map<Platform, @Size(min = 1, message = "version.edit.error.noPlatformVersions") SortedSet<@NotBlank(message = "version.new.error.invalidPlatformVersion") String>> platformDependencies;
    @NotBlank(message = "version.new.error.noDescription")
    private final String description;
    private final FileInfo fileInfo;
    @Validate(SpEL = "@validate.optionalRegex(#root, @hangarConfig.urlRegex)", message = "general.error.invalidUrl")
    private final String externalUrl;
    @NotBlank(message = "version.new.error.channel.noName")
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.channels.nameRegex)", message = "channel.new.error.invalidName")
    private final String channelName;
    @NotNull(message = "version.new.error.channel.noColor")
    private final Color channelColor;
    private final boolean channelNonReviewed;
    private final boolean forumSync;
    private final boolean unstable;
    private final boolean recommended;
    private final boolean isFile;

    @JsonCreator(mode = Mode.PROPERTIES)
    public PendingVersion(String versionString, Map<Platform, Set<PluginDependency>> pluginDependencies, EnumMap<Platform, SortedSet<String>> platformDependencies, String description, FileInfo fileInfo, String externalUrl, String channelName, Color channelColor, boolean channelNonReviewed, boolean forumSync, boolean unstable, boolean recommended, boolean isFile) {
        this.versionString = versionString;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        this.description = description;
        this.fileInfo = fileInfo;
        this.externalUrl = externalUrl;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.channelNonReviewed = channelNonReviewed;
        this.forumSync = forumSync;
        this.unstable = unstable;
        this.recommended = recommended;
        this.isFile = isFile;
    }

    public PendingVersion(String versionString, Map<Platform, Set<PluginDependency>> pluginDependencies, Map<Platform, SortedSet<String>> platformDependencies, String description, FileInfo fileInfo, ProjectChannelTable projectChannelTable, boolean forumSync) {
        this.versionString = versionString;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        this.description = "## " + this.versionString + (description != null ? "\n\n" + description : "");
        this.fileInfo = fileInfo;
        this.forumSync = forumSync;
        this.externalUrl = null;
        this.channelName = projectChannelTable.getName();
        this.channelColor = projectChannelTable.getColor();
        this.channelNonReviewed = projectChannelTable.isNonReviewed();
        this.unstable = false;
        this.recommended = false;
        this.isFile = true;
    }

    public PendingVersion(Map<Platform, Set<PluginDependency>> pluginDependencies, Map<Platform, SortedSet<String>> platformDependencies, String externalUrl, ProjectChannelTable projectChannelTable, boolean forumSync) {
        this.forumSync = forumSync;
        this.versionString = null;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        for (Platform platform : Platform.getValues()) {
            this.pluginDependencies.put(platform, new HashSet<>());
            this.platformDependencies.put(platform, new TreeSet<>());
        }
        this.description = null;
        this.fileInfo = null;
        this.externalUrl = externalUrl;
        this.channelName = projectChannelTable.getName();
        this.channelColor = projectChannelTable.getColor();
        this.channelNonReviewed = projectChannelTable.isNonReviewed();
        this.unstable = false;
        this.recommended = false;
        this.isFile = false;
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

    @Nullable
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public String getChannelName() {
        return channelName;
    }

    public Color getChannelColor() {
        return channelColor;
    }

    public boolean isChannelNonReviewed() {
        return channelNonReviewed;
    }

    public boolean isForumSync() {
        return forumSync;
    }

    public boolean isUnstable() {
        return unstable;
    }

    public boolean isRecommended() {
        return recommended;
    }

    @JsonProperty("isFile")
    public boolean isFile() {
        return isFile;
    }

    @Override
    public String toString() {
        return "PendingVersion{" +
                "versionString='" + versionString + '\'' +
                ", pluginDependencies=" + pluginDependencies +
                ", platformDependencies=" + platformDependencies +
                ", description='" + description + '\'' +
                ", fileInfo=" + fileInfo +
                ", externalUrl='" + externalUrl + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelColor=" + channelColor +
                ", channelNonReviewed=" + channelNonReviewed +
                ", forumSync=" + forumSync +
                ", unstable=" + unstable +
                ", recommended=" + recommended +
                ", isFile=" + isFile +
                '}';
    }
}

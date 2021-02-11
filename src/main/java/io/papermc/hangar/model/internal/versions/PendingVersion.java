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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingVersion {

    @NotBlank(message = "Must specify a version")
    @Validate(SpEL = "#root matches @hangarConfig.projects.versionNameRegex", message = "Version string contains invalid characters")
    private final String versionString;
    // TODO validate below by uncommenting the @Valid annotation
    private final Map<Platform, List</*@Valid */PluginDependency>> pluginDependencies;
    @Size(min = 1, max = 3, message = "Must specify between 1 and 3 platforms")
    private final Map<Platform, List<String>> platformDependencies;
    @NotBlank(message = "Must have a description")
    private final String description;
    private final FileInfo fileInfo;
    private final String externalUrl;
    @NotBlank(message = "Must have a channel name specified")
    @Validate(SpEL = "#root matches @hangarConfig.channels.nameRegex")
    private final String channelName;
    @NotNull(message = "Must have a channel color specified")
    private final Color channelColor;
    private final boolean channelNonReviewed;
    private final boolean forumSync;
    private final boolean unstable;
    private final boolean recommended;
    private final boolean isFile;

    @JsonCreator(mode = Mode.PROPERTIES)
    public PendingVersion(String versionString, Map<Platform, List<PluginDependency>> pluginDependencies, Map<Platform, List<String>> platformDependencies, String description, FileInfo fileInfo, String externalUrl, String channelName, Color channelColor, boolean channelNonReviewed, boolean forumSync, boolean unstable, boolean recommended, boolean isFile) {
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

    public PendingVersion(String versionString, Map<Platform, List<PluginDependency>> pluginDependencies, Map<Platform, List<String>> platformDependencies, String description, FileInfo fileInfo, ProjectChannelTable projectChannelTable, boolean forumSync) {
        this.versionString = versionString;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        this.description = "## " + this.versionString + "\n\n" + description;
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

    public PendingVersion(String externalUrl, ProjectChannelTable projectChannelTable, boolean forumSync) {
        this.forumSync = forumSync;
        this.versionString = null;
        this.pluginDependencies = new EnumMap<>(Platform.class);
        for (Platform platform : Platform.getValues()) {
            this.pluginDependencies.put(platform, new ArrayList<>());
        }
        this.platformDependencies = new HashMap<>();
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

    public Map<Platform, List<PluginDependency>> getPluginDependencies() {
        return pluginDependencies;
    }

    public Map<Platform, List<String>> getPlatformDependencies() {
        return platformDependencies;
    }

    public String getDescription() {
        return description;
    }

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

package io.papermc.hangar.service.pluginupload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.model.Color;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.generated.PlatformDependency;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.VersionDependencies;
import io.papermc.hangar.service.plugindata.PluginFileWithData;
import io.papermc.hangar.service.project.ProjectFactory;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class PendingVersion {

    private final String versionString;
    private final VersionDependencies dependencies;
    private final List<PlatformDependency> platforms;
    private final String description;
    private final long projectId;
    private final Long fileSize;
    private final String hash;
    private final String fileName;
    private final long authorId;
    private final String channelName;
    private final Color channelColor;
    private final PluginFileWithData plugin;
    private final String externalUrl;
    private final boolean createForumPost;

    public PendingVersion(String versionString, VersionDependencies dependencies, List<PlatformDependency> platforms, String description, long projectId, Long fileSize, String hash, String fileName, long authorId, String channelName, Color channelColor, PluginFileWithData plugin, String externalUrl, boolean createForumPost) {
        this.versionString = versionString;
        this.dependencies = dependencies;
        this.platforms = platforms;
        this.description = description;
        this.projectId = projectId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.authorId = authorId;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.plugin = plugin;
        this.externalUrl = externalUrl;
        this.createForumPost = createForumPost;
    }

    public String getVersionString() {
        return versionString;
    }

    public VersionDependencies getDependencies() {
        return dependencies;
    }

    public List<PlatformDependency> getPlatforms() {
        return platforms;
    }

    public String getDescription() {
        return description;
    }

    public long getProjectId() {
        return projectId;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getChannelName() {
        return channelName;
    }

    public Color getChannelColor() {
        return channelColor;
    }

    public PluginFileWithData getPlugin() {
        return plugin;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public boolean isCreateForumPost() {
        return createForumPost;
    }

    public List<Pair<Platform, ProjectVersionTagsTable>> getDependenciesAsGhostTags() {
        return Platform.getGhostTags(-1L, platforms);
    }

    public PendingVersion copy(String channelName, Color channelColor, boolean createForumPost, String description, List<PlatformDependency> platformDependencies) {
        return new PendingVersion(
                versionString,
                dependencies,
                platformDependencies,
                description,
                projectId,
                fileSize,
                hash,
                fileName,
                authorId,
                channelName,
                channelColor,
                plugin,
                externalUrl,
                createForumPost
        );
    }

    public ProjectVersionsTable complete(HttpServletRequest request, ProjectData project, ProjectFactory factory) {
        return factory.createVersion(request, project, this);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PendingVersion{");
        sb.append("versionString='").append(versionString).append('\'');
        sb.append(", dependencies=").append(dependencies);
        sb.append(", platforms=").append(platforms);
        sb.append(", description='").append(description).append('\'');
        sb.append(", projectId=").append(projectId);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", hash='").append(hash).append('\'');
        sb.append(", fileName='").append(fileName).append('\'');
        sb.append(", authorId=").append(authorId);
        sb.append(", channelName='").append(channelName).append('\'');
        sb.append(", channelColor=").append(channelColor);
        sb.append(", plugin=").append(plugin);
        sb.append(", externalUrl='").append(externalUrl).append('\'');
        sb.append(", createForumPost=").append(createForumPost);
        sb.append('}');
        return sb.toString();
    }
}

package io.papermc.hangar.serviceold.pluginupload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.controllerold.forms.NewVersion;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.modelold.generated.PlatformDependency;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.modelold.viewhelpers.VersionDependencies;
import io.papermc.hangar.service.internal.versions.plugindata.PluginFileWithData;
import io.papermc.hangar.serviceold.project.ProjectFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final ProjectVersionsTable prevVersion;

    public PendingVersion(@Nullable String versionString, @Nullable VersionDependencies dependencies, @Nullable List<PlatformDependency> platforms, @NotNull String description, long projectId, @Nullable Long fileSize, @Nullable String hash, @Nullable String fileName, long authorId, String channelName, Color channelColor, @Nullable PluginFileWithData plugin, @Nullable String externalUrl, boolean createForumPost, @Nullable ProjectVersionsTable prevVersion) {
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
        this.prevVersion = prevVersion;
    }

    @Nullable
    public String getVersionString() {
        return versionString;
    }

    @Nullable
    public VersionDependencies getDependencies() {
        return dependencies;
    }

    @Nullable
    public List<PlatformDependency> getPlatforms() {
        return platforms;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public long getProjectId() {
        return projectId;
    }

    @Nullable
    public Long getFileSize() {
        return fileSize;
    }

    @Nullable
    public String getHash() {
        return hash;
    }

    @Nullable
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

    @JsonIgnore
    public PluginFileWithData getPlugin() {
        return plugin;
    }

    @Nullable
    public String getExternalUrl() {
        return externalUrl;
    }

    public boolean isCreateForumPost() {
        return createForumPost;
    }

    @Nullable
    public ProjectVersionsTable getPrevVersion() {
        return prevVersion;
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
                createForumPost,
                prevVersion);
    }

    public PendingVersion update(NewVersion newVersion) {
        return new PendingVersion(
                versionString,
                newVersion.getVersionDependencies(),
                newVersion.getPlatformDependencies(),
                newVersion.getContent(),
                projectId,
                fileSize,
                hash,
                fileName,
                authorId,
                newVersion.getChannel().getName(),
                newVersion.getChannel().getColor(),
                plugin,
                newVersion.getExternalUrl(),
                newVersion.isForumSync(),
                prevVersion);
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

package me.minidigger.hangar.service.pluginupload;

import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.model.Color;
import me.minidigger.hangar.model.Platform;
import me.minidigger.hangar.model.generated.Dependency;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.service.VersionService;
import me.minidigger.hangar.service.plugindata.PluginFileWithData;
import me.minidigger.hangar.service.project.ProjectFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class PendingVersion {

    private final String versionString;
    private final List<Dependency> dependencies;
    private final String description;
    private final long projectId;
    private final long fileSize;
    private final String hash;
    private final String fileName;
    private final long authorId;
    private final String channelName;
    private final Color channelColor;
    private final PluginFileWithData plugin;
    private final boolean createForumPost;

    public PendingVersion(String versionString, List<Dependency> dependencies, String description, long projectId, long fileSize, String hash, String fileName, long authorId, String channelName, Color channelColor, PluginFileWithData plugin, boolean createForumPost) {
        this.versionString = versionString;
        this.dependencies = dependencies;
        this.description = description;
        this.projectId = projectId;
        this.fileSize = fileSize;
        this.hash = hash;
        this.fileName = fileName;
        this.authorId = authorId;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.plugin = plugin;
        this.createForumPost = createForumPost;
    }

    public String getVersionString() {
        return versionString;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public String getDescription() {
        return description;
    }

    public long getProjectId() {
        return projectId;
    }

    public long getFileSize() {
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

    public boolean isCreateForumPost() {
        return createForumPost;
    }

    public List<ProjectVersionTagsTable> getDependenciesAsGhostTags() {
        return Platform.getGhostTags(-1L, dependencies);
    }

    public PendingVersion copy(String channelName, Color channelColor, boolean createForumPost, String description) {
        return new PendingVersion(
                versionString,
                dependencies,
                description,
                projectId,
                fileSize,
                hash,
                fileName,
                authorId,
                channelName,
                channelColor,
                plugin,
                createForumPost
        );
    }

    public ProjectVersionsTable complete(HttpServletRequest request, ProjectData project, ProjectFactory factory) {
        return factory.createVersion(request, project, this);
    }
}

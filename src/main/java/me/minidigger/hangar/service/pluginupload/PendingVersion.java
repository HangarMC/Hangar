package me.minidigger.hangar.service.pluginupload;

import java.util.List;

import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.model.Color;
import me.minidigger.hangar.model.Platform;
import me.minidigger.hangar.model.generated.Dependency;
import me.minidigger.hangar.model.generated.Project;
import me.minidigger.hangar.service.plugindata.PluginFileData;
import me.minidigger.hangar.service.project.ProjectFactory;

public class PendingVersion {

    private String versionString;
    private List<Dependency> dependencies;
    private String description;
    private long projectId;
    private long fileSize;
    private String hash;
    private String fileName;
    private long authorId;
    private String channelName;
    private Color channelColor;
    private PluginFileData plugin;
    private boolean createForumPost;

    public PendingVersion(String versionString, List<Dependency> dependencies, String description, long projectId, long fileSize, String hash, String fileName, long authorId, String channelName, Color channelColor, PluginFileData plugin, boolean createForumPost) {
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

    public PluginFileData getPlugin() {
        return plugin;
    }

    public boolean isCreateForumPost() {
        return createForumPost;
    }

    public List<ProjectVersionTagsTable> getDependenciesAsGhostTags() {
        return Platform.getGhostTags(-1L, dependencies);
    }

    public void complete(Project project, ProjectFactory factory) {
        factory.createVersion(project, this);
    }
}

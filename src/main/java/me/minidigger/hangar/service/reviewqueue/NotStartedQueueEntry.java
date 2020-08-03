package me.minidigger.hangar.service.reviewqueue;

import me.minidigger.hangar.model.Color;
import me.minidigger.hangar.model.generated.ProjectNamespace;

import java.time.OffsetDateTime;

public class NotStartedQueueEntry {

    private ProjectNamespace namespace;
    private String projectName;
    private String versionString;
    private OffsetDateTime versionCreatedAt;
    private String channelName;
    private Color channelColor;
    private String versionAuthor;

    public NotStartedQueueEntry(ProjectNamespace namespace, String projectName, String versionString, OffsetDateTime versionCreatedAt, String channelName, Color channelColor, String versionAuthor) {
        this.namespace = namespace;
        this.projectName = projectName;
        this.versionString = versionString;
        this.versionCreatedAt = versionCreatedAt;
        this.channelName = channelName;
        this.channelColor = channelColor;
        this.versionAuthor = versionAuthor;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersionString() {
        return versionString;
    }

    public OffsetDateTime getVersionCreatedAt() {
        return versionCreatedAt;
    }

    public String getChannelName() {
        return channelName;
    }

    public Color getChannelColor() {
        return channelColor;
    }

    public String getVersionAuthor() {
        return versionAuthor;
    }
}

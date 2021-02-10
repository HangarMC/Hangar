package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PendingVersion {

    private final String versionString;
    private final Map<Platform, List<PluginDependency>> pluginDependencies;
    private final List<PlatformDependency> platformDependencies;
    private final String description;
    private final FileInfo fileInfo;
    private final String externalUrl;
    private final String channelName;
    private final Color channelColor;
    private final boolean forumSync;
    private final boolean isFile;

    public PendingVersion(String versionString, Map<Platform, List<PluginDependency>> pluginDependencies, List<PlatformDependency> platformDependencies, String description, FileInfo fileInfo, ProjectChannelTable projectChannelTable, boolean forumSync) {
        this.versionString = versionString;
        this.pluginDependencies = pluginDependencies;
        this.platformDependencies = platformDependencies;
        this.description = description;
        this.fileInfo = fileInfo;
        this.forumSync = forumSync;
        this.externalUrl = null;
        this.channelName = projectChannelTable.getName();
        this.channelColor = projectChannelTable.getColor();
        this.isFile = true;
    }

    public PendingVersion(String externalUrl, ProjectChannelTable projectChannelTable, boolean forumSync) {
        this.forumSync = forumSync;
        this.versionString = null;
        this.pluginDependencies = new EnumMap<>(Platform.class);
        for (Platform platform : Platform.getValues()) {
            this.pluginDependencies.put(platform, new ArrayList<>());
        }
        this.platformDependencies = new ArrayList<>();
        this.description = null;
        this.fileInfo = null;
        this.externalUrl = externalUrl;
        this.channelName = projectChannelTable.getName();
        this.channelColor = projectChannelTable.getColor();
        this.isFile = false;
    }

    public String getVersionString() {
        return versionString;
    }

    public Map<Platform, List<PluginDependency>> getPluginDependencies() {
        return pluginDependencies;
    }

    public List<PlatformDependency> getPlatformDependencies() {
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
                ", forumSync=" + forumSync +
                ", isFile=" + isFile +
                '}';
    }

    @JsonComponent
    public static class PendingVersionSerializer extends JsonSerializer<PendingVersion> {
        @Override
        public void serialize(PendingVersion value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("versionString", value.versionString);
            gen.writeFieldName("pluginDependencies");
            serializers.defaultSerializeValue(value.pluginDependencies, gen);
            gen.writeObjectFieldStart("platformDependencies");
            for (PlatformDependency platformDependency : value.platformDependencies) {
                gen.writeArrayFieldStart(platformDependency.getPlatform().name());
                for (String version : platformDependency.getVersions()) {
                    gen.writeString(version);
                }
                gen.writeEndArray();
            }
            gen.writeEndObject();
            gen.writeStringField("description", value.description);
            gen.writeFieldName("fileInfo");
            serializers.defaultSerializeValue(value.fileInfo, gen);
            gen.writeStringField("externalUrl", value.externalUrl);
            gen.writeStringField("channelName", value.channelName);
            gen.writeStringField("channelColor", value.channelColor.getHex());
            gen.writeBooleanField("forumSync", value.forumSync);
            gen.writeBooleanField("isFile", value.isFile);
        }
    }
}

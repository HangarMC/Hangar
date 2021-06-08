package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.FileTypeHandler.FileData;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

public class PluginFileData {

    private final String version;
    private final String name;
    private final String description;
    private final Map<Platform, Set<String>> authors;
    private final Map<Platform, Set<PluginDependency>> pluginDependencies;
    private final Map<Platform, SortedSet<String>> platformDependencies;

    public PluginFileData(Map<Platform, FileData> fileDataMap) {
        this.version = fileDataMap.values().stream().map(FileData::getVersion).filter(Objects::nonNull).findAny().orElse(null);
        this.name = fileDataMap.values().stream().map(FileData::getName).filter(Objects::nonNull).findAny().orElse(null);
        this.description = fileDataMap.values().stream().map(FileData::getDescription).filter(Objects::nonNull).findAny().orElse(null);
        this.authors = new EnumMap<>(Platform.class);
        this.pluginDependencies = new EnumMap<>(Platform.class);
        this.platformDependencies = new EnumMap<>(Platform.class);
        fileDataMap.forEach((platform, fileData) -> {
            this.authors.put(platform, fileData.getAuthors());
            this.pluginDependencies.put(platform, fileData.getPluginDependencies());
            this.platformDependencies.put(platform, fileData.getPlatformDependencies());
        });
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nullable
    public String getVersion() {
        return this.version;
    }

    @NotNull
    public Map<Platform, Set<String>> getAuthors() {
        return this.authors;
    }

    @NotNull
    public Map<Platform, Set<PluginDependency>> getDependencies() {
        return this.pluginDependencies;
    }

    @NotNull
    public Map<Platform, SortedSet<String>> getPlatformDependencies() {
        return this.platformDependencies;
    }

    public void validate() {
        if (getName() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.incomplete", "name");
        } else if (getVersion() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.incomplete", "version");
        } else if (getPlatformDependencies().isEmpty()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.incomplete", "platform");
        }
    }
}

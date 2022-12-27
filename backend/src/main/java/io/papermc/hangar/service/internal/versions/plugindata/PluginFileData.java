package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.FileTypeHandler;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public class PluginFileData {

    private final String version;
    private final String name;
    private final String description;
    private final Map<Platform, Set<String>> authors;
    private final Map<Platform, Set<PluginDependency>> pluginDependencies;
    private final Map<Platform, SortedSet<String>> platformDependencies;

    public PluginFileData(final Map<Platform, FileTypeHandler.FileData> fileDataMap) {
        this.version = fileDataMap.values().stream().map(FileTypeHandler.FileData::getVersion).filter(Objects::nonNull).findAny().orElse(null);
        this.name = fileDataMap.values().stream().map(FileTypeHandler.FileData::getName).filter(Objects::nonNull).findAny().orElse(null);
        this.description = fileDataMap.values().stream().map(FileTypeHandler.FileData::getDescription).filter(Objects::nonNull).findAny().orElse(null);
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

    public @NotNull Map<Platform, Set<String>> getAuthors() {
        return this.authors;
    }

    public @NotNull Map<Platform, Set<PluginDependency>> getDependencies() {
        return this.pluginDependencies;
    }

    public @NotNull Map<Platform, SortedSet<String>> getPlatformDependencies() {
        return this.platformDependencies;
    }

    private static final String INCOMPLETE_MSG = "version.new.error.incomplete";

    public void validate() {
        if (this.getName() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, INCOMPLETE_MSG, "name");
        } else if (this.getVersion() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, INCOMPLETE_MSG, "version");
        } else if (this.getPlatformDependencies().isEmpty()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, INCOMPLETE_MSG, "platform");
        }
    }
}

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
//    private final Map<String, DataValue> dataValues = new HashMap<>();

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

//    public PluginFileData(Map<Platform, List<DataValue>> dataValues) {
//        dataValues.forEach((platform, values) -> {
//            for (DataValue value : values) {
//                switch (value.getKey()) {
//                    case FileTypeHandler.DEPENDENCIES:
//                        if (this.dataValues.containsKey(value.getKey())) {
//                            DependencyDataValue dependencyDataValue = (DependencyDataValue) this.dataValues.get(value.getKey());
//                            dependencyDataValue.getValue().putAll(((DependencyDataValue) value).getValue());
//                        } else {
//                            this.dataValues.put(value.getKey(), value);
//                        }
//                        break;
//                    case FileTypeHandler.PLATFORM_DEPENDENCY:
//                        if (this.dataValues.containsKey(value.getKey())) {
//                            PlatformDependencyDataValue platformDependencyDataValue = (PlatformDependencyDataValue) this.dataValues.get(value.getKey());
//                            platformDependencyDataValue.getValue().putAll(((PlatformDependencyDataValue) value).getValue());
//                        } else {
//                            PlatformDependencyDataValue platformDependencyDataValue = (PlatformDependencyDataValue) value;
//                            this.dataValues.put(value.getKey(), new PlatformDependencyDataValue(value.getKey(), new HashMap<>(platformDependencyDataValue.getValue())));
//                        }
//                        break;
//                    default:
//                        this.dataValues.put(value.getKey(), value);
//                }
//            }
//        });
//    }

    @Nullable
    public String getName() {
        return this.name;
//        DataValue name = dataValues.get("name");
//        return name != null ? ((StringDataValue) name).getValue() :  null;
    }

    @Nullable
    public String getDescription() {
        return this.description;
//        DataValue description = dataValues.get("description");
//        return description != null ? ((StringDataValue) description).getValue() :  null;
    }

    @Nullable
    public String getVersion() {
        return this.version;
//        DataValue version = dataValues.get("version");
//        return version != null ? ((StringDataValue) version).getValue() :  null;
    }

    @NotNull
    public Map<Platform, Set<String>> getAuthors() {
        return this.authors;
//        DataValue authors = dataValues.get("authors");
//        return authors != null ? ((StringListDataValue) authors).getValue() :  null;
    }

    @NotNull
    public Map<Platform, Set<PluginDependency>> getDependencies() {
//        DataValue dependencies = dataValues.get("dependencies");
//        if (dependencies == null) {
            // compiler needs the types here - Jake
            //noinspection Convert2Diamond
//            return new EnumMap<Platform, Set<PluginDependency>>(getPlatformDependencies().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new HashSet<>())));
//        }
//        return ((DependencyDataValue) dependencies).getValue();
        return this.pluginDependencies;
    }

    @NotNull
    public Map<Platform, SortedSet<String>> getPlatformDependencies() {
        // DataValue platformDependencies = dataValues.get(FileTypeHandler.PLATFORM_DEPENDENCY);
        // return platformDependencies != null ? ((PlatformDependencyDataValue) platformDependencies).getValue() : null;
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

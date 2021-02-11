package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.FileTypeHandler;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static io.papermc.hangar.service.internal.versions.plugindata.DataValue.DependencyDataValue;
import static io.papermc.hangar.service.internal.versions.plugindata.DataValue.PlatformDependencyDataValue;
import static io.papermc.hangar.service.internal.versions.plugindata.DataValue.StringDataValue;
import static io.papermc.hangar.service.internal.versions.plugindata.DataValue.StringListDataValue;

public class PluginFileData {
    private final Map<String, DataValue> dataValues = new HashMap<>();

    public PluginFileData(Map<Platform, List<DataValue>> dataValues) {
        dataValues.forEach((platform, values) -> {
            for (DataValue value : values) {
                switch (value.getKey()) {
                    case FileTypeHandler.DEPENDENCIES:
                        if (this.dataValues.containsKey(value.getKey())) {
                            DependencyDataValue dependencyDataValue = (DependencyDataValue) this.dataValues.get(value.getKey());
                            dependencyDataValue.getValue().putAll(((DependencyDataValue) value).getValue());
                        } else {
                            this.dataValues.put(value.getKey(), value);
                        }
                        break;
                    case FileTypeHandler.PLATFORM_DEPENDENCY:
                        if (this.dataValues.containsKey(value.getKey())) {
                            PlatformDependencyDataValue platformDependencyDataValue = (PlatformDependencyDataValue) this.dataValues.get(value.getKey());
                            platformDependencyDataValue.getValue().putAll(((PlatformDependencyDataValue) value).getValue());
                        } else {
                            PlatformDependencyDataValue platformDependencyDataValue = (PlatformDependencyDataValue) value;
                            this.dataValues.put(value.getKey(), new PlatformDependencyDataValue(value.getKey(), new HashMap<>(platformDependencyDataValue.getValue())));
                        }
                        break;
                    default:
                        this.dataValues.put(value.getKey(), value);
                }
            }
        });
    }

    @Nullable
    public String getName() {
        DataValue name = dataValues.get("name");
        return name != null ? ((StringDataValue) name).getValue() :  null;
    }

    @Nullable
    public String getDescription() {
        DataValue description = dataValues.get("description");
        return description != null ? ((StringDataValue) description).getValue() :  null;
    }

    @Nullable
    public String getWebsite() {
        DataValue url = dataValues.get("url");
        return url != null ? ((StringDataValue) url).getValue() :  null;
    }

    @Nullable
    public String getVersion() {
        DataValue version = dataValues.get("version");
        return version != null ? ((StringDataValue) version).getValue() :  null;
    }

    @Nullable
    public List<String> getAuthors() {
        DataValue authors = dataValues.get("authors");
        return authors != null ? ((StringListDataValue) authors).getValue() :  null;
    }

    @Nullable
    public Map<Platform, List<PluginDependency>> getDependencies() {
        DataValue dependencies = dataValues.get("dependencies");
        if (dependencies == null) {
            // compiler needs the types here - Jake
            //noinspection Convert2Diamond
            return new EnumMap<Platform, List<PluginDependency>>(getPlatformDependencies().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new ArrayList<>())));
        }
        return ((DependencyDataValue) dependencies).getValue();
    }

    public Map<Platform, List<String>> getPlatformDependencies() {
        DataValue platformDependencies = dataValues.get(FileTypeHandler.PLATFORM_DEPENDENCY);
        return platformDependencies != null ? ((PlatformDependencyDataValue) platformDependencies).getValue() : null;
    }

    public void validate() {
        if (getName() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.incomplete", "name");
        } else if (getVersion() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.incomplete", "version");
        } else if (getPlatformDependencies() == null || getPlatformDependencies().isEmpty()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "version.new.error.incomplete", "platform");
        }
    }
}

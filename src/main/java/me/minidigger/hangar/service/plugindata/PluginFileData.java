package me.minidigger.hangar.service.plugindata;

import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.service.VersionService;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.minidigger.hangar.model.generated.Dependency;

import static me.minidigger.hangar.service.plugindata.DataValue.*;

public class PluginFileData {
    private final Map<String, DataValue> dataValues = new HashMap<>();

    public PluginFileData(List<DataValue> dataValues) {
        for (DataValue dataValue : dataValues) {
            this.dataValues.put(dataValue.getKey(), dataValue);
        }
    }

    @Nullable
    public UUID getId() {
        DataValue id = dataValues.get("id");
        return id != null ? ((UUIDDataValue) id).getValue() : null;
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
    public List<Dependency> getDependencies() {
        DataValue dependencies = dataValues.get("dependencies");
        return dependencies != null ? ((DependencyDataValue) dependencies).getValue() :  null;
    }

    public boolean validate() {
        return getId() != null && getName() != null && getAuthors() != null && getDependencies() != null;
    }

    public List<ProjectVersionTagsTable> createTags(long versionId, VersionService versionService) {
        // TODO not sure what is happening here in ore... it seems to only add tags if they contain mixins?
        return new ArrayList<>();
    }
}

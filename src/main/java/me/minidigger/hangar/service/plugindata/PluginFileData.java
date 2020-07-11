package me.minidigger.hangar.service.plugindata;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.minidigger.hangar.model.Dependency;

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
        if (!dataValues.containsKey("id")) return null;
        return ((UUIDDataValue) dataValues.get("id")).getValue();
    }

    @Nullable
    public String getName() {
        if (!dataValues.containsKey("name")) return null;
        return ((StringDataValue) dataValues.get("name")).getValue();
    }

    @Nullable
    public String getDescription() {
        if (!dataValues.containsKey("description")) return null;
        return ((StringDataValue) dataValues.get("description")).getValue();
    }

    @Nullable
    public String getWebsite() {
        if (!dataValues.containsKey("url")) return null;
        return ((StringDataValue) dataValues.get("url")).getValue();
    }

    @Nullable
    public String getVersion() {
        if (!dataValues.containsKey("version")) return null;
        return ((StringDataValue) dataValues.get("version")).getValue();
    }

    @Nullable
    public List<String> getAuthors() {
        if (!dataValues.containsKey("authors")) return null;
        return ((StringListDataValue) dataValues.get("authors")).getValue();
    }

    @Nullable
    public List<Dependency> getDependencies() {
        if (!dataValues.containsKey("dependencies")) return null;
        return ((DependencyDataValue) dataValues.get("dependencies")).getValue();
    }

    public boolean validate() {
        return getId() != null && getName() != null && getAuthors() != null && getDependencies() != null;
    }
}

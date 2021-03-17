package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.DataValue;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class VelocityFileHandler extends FileTypeHandler {

    protected VelocityFileHandler() {
        super("velocity-plugin.json");
    }

    @Override
    public List<DataValue> getData(BufferedReader reader) {
        List<DataValue> result = new ArrayList<>();

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(reader);
        if (data == null || data.isEmpty()) {
            return result;
        }

        if (data.containsKey("version")) {
            String version = String.valueOf(data.get("version"));
            if (version != null) {
                result.add(new DataValue.StringDataValue(FileTypeHandler.VERSION, version));
            }
        }
        String name = (String) data.get("name");
        if (name != null) {
            result.add(new DataValue.StringDataValue(FileTypeHandler.NAME, name));
        }
        String description = (String) data.get("description");
        if (description != null) {
            result.add(new DataValue.StringDataValue(FileTypeHandler.DESCRIPTION, description));
        }
        String url = (String) data.get("url");
        if (url != null) {
            result.add(new DataValue.StringDataValue(FileTypeHandler.URL, url));
        }
        String author = (String) data.get("author");
        if (author != null) {
            result.add(new DataValue.StringListDataValue(FileTypeHandler.AUTHORS, List.of(author)));
        }
        //noinspection unchecked
        List<String> authors = (List<String>) data.get("authors");
        if (authors != null) {
            result.add(new DataValue.StringListDataValue(FileTypeHandler.AUTHORS, authors));
        }
        Set<PluginDependency> dependencies;
        //noinspection unchecked
        List<Map<String, Object>> deps = (List<Map<String, Object>>) data.get("dependencies");
        if (deps != null) {
            dependencies = deps.stream().map(dep -> new PluginDependency((String) dep.get("id"), !(boolean) dep.getOrDefault("optional", false), null, null)).collect(Collectors.toSet());
        } else {
            dependencies = new HashSet<>();
        }

        if (!dependencies.isEmpty()) {
            result.add(new DataValue.DependencyDataValue(FileTypeHandler.DEPENDENCIES, getPlatform(), dependencies));
        }

        result.add(new DataValue.PlatformDependencyDataValue(FileTypeHandler.PLATFORM_DEPENDENCY, getPlatform(), new TreeSet<>()));
        return result;
    }

    @Override
    public Platform getPlatform() {
        return Platform.VELOCITY;
    }
}

package io.papermc.hangar.service.plugindata.handler;

import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.model.generated.PlatformDependency;
import io.papermc.hangar.service.plugindata.DataValue;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        List<Dependency> dependencies;
        //noinspection unchecked
        List<Map<String, Object>> deps = (List<Map<String, Object>>) data.get("dependencies");
        if (deps != null) {
            dependencies = deps.stream().map(dep -> new Dependency((String) dep.get("id"), !(boolean) dep.getOrDefault("optional", false))).collect(Collectors.toList());
        } else {
            dependencies = new ArrayList<>();
        }

        if (!dependencies.isEmpty()) {
            result.add(new DataValue.DependencyDataValue(FileTypeHandler.DEPENDENCIES, getPlatform(), dependencies));
        }

        result.add(new DataValue.PlatformDependencyDataValue(FileTypeHandler.PLATFORM_DEPENDENCY, new PlatformDependency(getPlatform(), new ArrayList<>())));
        return result;
    }

    @Override
    public Platform getPlatform() {
        return Platform.VELOCITY;
    }
}

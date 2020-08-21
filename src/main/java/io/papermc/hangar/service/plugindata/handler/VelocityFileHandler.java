package io.papermc.hangar.service.plugindata.handler;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.service.plugindata.DataValue;

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

        String version = (String) data.get("version");
        if (version != null) {
            result.add(new DataValue.StringDataValue("version", version));
        }
        String name = (String) data.get("name");
        if (name != null) {
            result.add(new DataValue.StringDataValue("name", name));
        }
        String description = (String) data.get("description");
        if (description != null) {
            result.add(new DataValue.StringDataValue("description", description));
        }
        String url = (String) data.get("url");
        if (url != null) {
            result.add(new DataValue.StringDataValue("url", url));
        }
        String author = (String) data.get("author");
        if (author != null) {
            result.add(new DataValue.StringListDataValue("authors", List.of(author)));
        }
        //noinspection unchecked
        List<String> authors = (List<String>) data.get("authors");
        if (authors != null) {
            result.add(new DataValue.StringListDataValue("authors", authors));
        }
        List<Dependency> dependencies;
        //noinspection unchecked
        List<Map<String, Object>> deps = (List<Map<String, Object>>) data.get("dependencies");
        if (deps != null) {
            dependencies = deps.stream().map(p -> new Dependency((String) p.get("id"), null, !(boolean) p.getOrDefault("optional", false))).collect(Collectors.toList());
        } else {
            dependencies = new ArrayList<>();
        }
        dependencies.add(new Dependency("velocity", null));
        result.add(new DataValue.DependencyDataValue("dependencies", dependencies));

        return result;
    }
}

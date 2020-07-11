package me.minidigger.hangar.service.plugindata.handler;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.minidigger.hangar.model.Dependency;
import me.minidigger.hangar.service.plugindata.DataValue;

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
        if (data == null || data.size() == 0) {
            return result;
        }

        if (data.containsKey("version")) {
            result.add(new DataValue.StringDataValue("version", (String) data.get("version")));
        }
        if (data.containsKey("name")) {
            result.add(new DataValue.StringDataValue("name", (String) data.get("name")));
        }
        if (data.containsKey("description")) {
            result.add(new DataValue.StringDataValue("description", (String) data.get("description")));
        }
        if (data.containsKey("url")) {
            result.add(new DataValue.StringDataValue("url", (String) data.get("url")));
        }
        if (data.containsKey("author")) {
            result.add(new DataValue.StringListDataValue("authors", List.of((String) data.get("author"))));
        }
        if (data.containsKey("authors")) {
            //noinspection unchecked
            result.add(new DataValue.StringListDataValue("authors", (List<String>) data.get("authors")));
        }
        List<Dependency> dependencies;
        if (data.containsKey("dependencies")) {
            //noinspection unchecked
            List<Map<String, Object>> deps = (List<Map<String, Object>>) data.get("dependencies");
            dependencies = deps.stream().map(p -> new Dependency((String) p.get("id"), null, !(boolean) p.getOrDefault("optional", false))).collect(Collectors.toList());
        } else {
            dependencies = new ArrayList<>();
        }
        dependencies.add(new Dependency("velocity", null));
        result.add(new DataValue.DependencyDataValue("dependencies", dependencies));

        return result;
    }
}

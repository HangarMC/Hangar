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
import me.minidigger.hangar.service.plugindata.DataValue.DependencyDataValue;
import me.minidigger.hangar.service.plugindata.DataValue.StringDataValue;
import me.minidigger.hangar.service.plugindata.DataValue.StringListDataValue;

@Component
public class PaperPluginFileHandler extends FileTypeHandler {

    protected PaperPluginFileHandler() {
        super("plugin.yml");
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
            result.add(new StringDataValue("version", (String) data.get("version")));
        }
        if (data.containsKey("name")) {
            result.add(new StringDataValue("name", (String) data.get("name")));
        }
        if (data.containsKey("description")) {
            result.add(new StringDataValue("description", (String) data.get("description")));
        }
        if (data.containsKey("website")) {
            result.add(new StringDataValue("url", (String) data.get("website")));
        }
        if (data.containsKey("author")) {
            result.add(new DataValue.StringListDataValue("authors", List.of((String) data.get("author"))));
        }
        if (data.containsKey("authors")) {
            //noinspection unchecked
            result.add(new StringListDataValue("authors", (List<String>) data.get("authors")));
        }
        List<Dependency> dependencies = new ArrayList<>();
        if (data.containsKey("softdepend")) {
            //noinspection unchecked
            dependencies.addAll(((List<String>) data.get("softdepend")).stream().map(p -> new Dependency(p, null, false)).collect(Collectors.toList()));
        }
        if (data.containsKey("depend")) {
            //noinspection unchecked
            dependencies.addAll(((List<String>) data.get("depend")).stream().map(p -> new Dependency(p, null)).collect(Collectors.toList()));
        }

        String paperVersion = data.getOrDefault("api-version", "").toString();
        Dependency paperDependency = new Dependency("paperapi", paperVersion.length() > 0 ? paperVersion : null);
        dependencies.add(paperDependency);
        result.add(new DependencyDataValue("dependencies", dependencies));

        return result;
    }
}

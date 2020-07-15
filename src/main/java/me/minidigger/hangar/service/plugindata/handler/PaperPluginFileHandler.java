package me.minidigger.hangar.service.plugindata.handler;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.minidigger.hangar.model.generated.Dependency;
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
        if (data == null || data.isEmpty()) {
            return result;
        }

        String version = (String) data.get("version");
        if (version != null) {
            result.add(new StringDataValue("version", version));
        }
        String name = (String) data.get("name");
        if (name != null) {
            result.add(new StringDataValue("name", name));
        }
        String description = (String) data.get("description");
        if (description != null) {
            result.add(new StringDataValue("description", description));
        }
        String website = (String) data.get("website");
        if (website != null) {
            result.add(new StringDataValue("url", website));
        }
        String author = (String) data.get("author");
        if (author != null) {
            result.add(new DataValue.StringListDataValue("authors", List.of(author)));
        }
        List<String> authors = (List<String>) data.get("authors");
        if (authors != null) {
            result.add(new StringListDataValue("authors", authors));
        }

        List<Dependency> dependencies = new ArrayList<>();
        //noinspection unchecked
        List<String> softdepend = (List<String>) data.get("softdepend");
        if (softdepend != null) {
            dependencies.addAll(softdepend.stream().map(p -> new Dependency(p, null, false)).collect(Collectors.toList()));
        }
        //noinspection unchecked
        List<String> depend = (List<String>) data.get("depend");
        if (depend != null) {
            dependencies.addAll(depend.stream().map(p -> new Dependency(p, null)).collect(Collectors.toList()));
        }

        String paperVersion = data.getOrDefault("api-version", "").toString();
        Dependency paperDependency = new Dependency("paperapi", !paperVersion.isEmpty() ? paperVersion : null);
        dependencies.add(paperDependency);
        result.add(new DependencyDataValue("dependencies", dependencies));

        return result;
    }
}

package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.DataValue;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WaterfallPluginFileHandler extends FileTypeHandler {

    protected WaterfallPluginFileHandler() {
        super("bungee.yml");
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
        String website = (String) data.get("website");
        if (website != null) {
            result.add(new DataValue.StringDataValue(FileTypeHandler.URL, website));
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

        List<PluginDependency> dependencies = new ArrayList<>();
        //noinspection unchecked
        List<String> softdepend = (List<String>) data.get("softDepends");
        if (softdepend != null) {
            dependencies.addAll(softdepend.stream().map(depName -> new PluginDependency(depName, false, null, null)).collect(Collectors.toList()));
        }
        //noinspection unchecked
        List<String> depend = (List<String>) data.get("depends");
        if (depend != null) {
            dependencies.addAll(depend.stream().map(depName -> new PluginDependency(depName, true, null, null)).collect(Collectors.toList()));
        }

        if (!dependencies.isEmpty()) {
            result.add(new DataValue.DependencyDataValue(FileTypeHandler.DEPENDENCIES, getPlatform(), dependencies));
        }

        result.add(new DataValue.PlatformDependencyDataValue(FileTypeHandler.PLATFORM_DEPENDENCY, getPlatform(), new ArrayList<>()));
        return result;
    }

    @Override
    public Platform getPlatform() {
        return Platform.WATERFALL;
    }
}

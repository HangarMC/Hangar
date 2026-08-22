package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.PaperFileTypeHandler.PaperFileData;
import java.io.BufferedReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.springframework.stereotype.Component;

@Component
public class PaperFileTypeHandler extends FileTypeHandler<PaperFileData> {

    protected PaperFileTypeHandler() {
        super("paper-plugin.yml", Platform.PAPER);
    }

    @Override
    public PaperFileData getData(final BufferedReader reader) throws ConfigurateException {
        return YamlConfigurationLoader.builder().buildAndLoadString(reader.lines().collect(Collectors.joining("\n"))).get(PaperFileData.class);
    }

    @ConfigSerializable
    public static class PaperFileData extends FileData {

        @Setting("dependencies")
        private Map<String, Map<String, Dependency>> dependencies;
        @Setting("api-version")
        private String apiVersion;

        @Override
        public SortedSet<String> getPlatformDependencies() {
            final SortedSet<String> platformVersions = new TreeSet<>();
            if (this.apiVersion != null) {
                platformVersions.add(this.apiVersion);
            }
            return platformVersions;
        }

        @Override
        protected Set<PluginDependency> createPluginDependencies() {
            final Set<PluginDependency> dependencies = new HashSet<>();
            if (this.dependencies != null) {
                for (final Map.Entry<String, Map<String, Dependency>> entry : this.dependencies.entrySet()) {
                    for (final Map.Entry<String, Dependency> dependencyEntry : entry.getValue().entrySet()) {
                        final String dependencyName = dependencyEntry.getKey();
                        final boolean required = dependencyEntry.getValue().required;
                        dependencies.add(PluginDependency.of(dependencyName, required, Platform.PAPER));
                    }
                }
            }
            return dependencies;
        }

        @ConfigSerializable
        record Dependency(boolean required) {}
    }
}

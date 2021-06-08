package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.PaperFileTypeHandler.PaperFileData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class PaperFileTypeHandler extends FileTypeHandler<PaperFileData> {

    protected PaperFileTypeHandler() {
        super("plugin.yml", Platform.PAPER);
    }

    @Override
    public PaperFileData getData(BufferedReader reader) throws ConfigurateException {
        ConfigurationNode node = YamlConfigurationLoader.builder().buildAndLoadString(reader.lines().collect(Collectors.joining("\n")));
        return node.get(PaperFileData.class);
    }

    @ConfigSerializable
    public static class PaperFileData extends FileData {

        @Setting("depend")
        private List<String> hardDepends;
        @Setting("softdepend")
        private List<String> softDepends;
        @Setting("api-version")
        private String apiVersion;

        @NotNull
        @Override
        public SortedSet<String> getPlatformDependencies() {
            SortedSet<String> platformVersions = new TreeSet<>();
            if (this.apiVersion != null) {
                platformVersions.add(this.apiVersion);
            }
            return platformVersions;
        }

        @NotNull
        @Override
        protected Set<PluginDependency> createPluginDependencies() {
            Set<PluginDependency> dependencies = new HashSet<>();
            if (hardDepends != null) {
                for (String hardDepend : hardDepends) {
                    dependencies.add(PluginDependency.of(hardDepend, true));
                }
            }
            if (softDepends != null) {
                for (String softDepend : softDepends) {
                    dependencies.add(PluginDependency.of(softDepend, false));
                }
            }
            return dependencies;
        }
    }
}

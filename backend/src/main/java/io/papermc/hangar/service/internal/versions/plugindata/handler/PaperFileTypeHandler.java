package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.PaperFileTypeHandler.PaperFileData;
import java.io.BufferedReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.springframework.stereotype.Component;

@Component
public class PaperFileTypeHandler extends FileTypeHandler<PaperFileData> {

    protected PaperFileTypeHandler() {
        super("plugin.yml", Platform.PAPER);
    }

    @Override
    public PaperFileData getData(final BufferedReader reader) throws ConfigurateException {
        return YamlConfigurationLoader.builder().buildAndLoadString(reader.lines().collect(Collectors.joining("\n"))).get(PaperFileData.class);
    }

    @ConfigSerializable
    public static class PaperFileData extends FileTypeHandler.FileData {

        @Setting("depend")
        private List<String> hardDepends;
        @Setting("softdepend")
        private List<String> softDepends;
        @Setting("api-version")
        private String apiVersion;

        @Override
        public @NotNull SortedSet<String> getPlatformDependencies() {
            final SortedSet<String> platformVersions = new TreeSet<>();
            if (this.apiVersion != null) {
                platformVersions.add(this.apiVersion);
            }
            return platformVersions;
        }

        @Override
        protected @NotNull Set<PluginDependency> createPluginDependencies() {
            final Set<PluginDependency> dependencies = new HashSet<>();
            if (this.hardDepends != null) {
                for (final String hardDepend : this.hardDepends) {
                    dependencies.add(PluginDependency.of(hardDepend, true, Platform.PAPER));
                }
            }
            if (this.softDepends != null) {
                for (final String softDepend : this.softDepends) {
                    dependencies.add(PluginDependency.of(softDepend, false, Platform.PAPER));
                }
            }
            return dependencies;
        }
    }
}

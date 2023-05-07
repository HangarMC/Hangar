package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.WaterfallFileTypeHandler.WaterfallFileData;
import java.io.BufferedReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.springframework.stereotype.Component;

@Component
public class WaterfallFileTypeHandler extends FileTypeHandler<WaterfallFileData> {

    protected WaterfallFileTypeHandler() {
        super("bungee.yml", Platform.WATERFALL);
    }

    @Override
    public WaterfallFileData getData(final BufferedReader reader) throws ConfigurateException {
        return YamlConfigurationLoader.builder().buildAndLoadString(reader.lines().collect(Collectors.joining("\n"))).get(WaterfallFileData.class);
    }

    @ConfigSerializable
    public static class WaterfallFileData extends FileTypeHandler.FileData {

        @Setting("depends")
        private List<String> hardDepends;
        @Setting("softDepends")
        private List<String> softDepends;

        @Override
        protected @NotNull Set<PluginDependency> createPluginDependencies() {
            final Set<PluginDependency> dependencies = new HashSet<>();
            if (this.hardDepends != null) {
                for (final String hardDepend : this.hardDepends) {
                    dependencies.add(PluginDependency.of(hardDepend, true, Platform.WATERFALL));
                }
            }
            if (this.softDepends != null) {
                for (final String softDepend : this.softDepends) {
                    dependencies.add(PluginDependency.of(softDepend, false, Platform.WATERFALL));
                }
            }
            return dependencies;
        }
    }
}

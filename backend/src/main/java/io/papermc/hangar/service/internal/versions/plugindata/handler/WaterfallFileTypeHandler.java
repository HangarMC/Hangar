package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.WaterfallFileTypeHandler.WaterfallFileData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WaterfallFileTypeHandler extends FileTypeHandler<WaterfallFileData> {

    protected WaterfallFileTypeHandler() {
        super("bungee.yml", Platform.WATERFALL);
    }

    @Override
    public WaterfallFileData getData(BufferedReader reader) throws ConfigurateException {
        return YamlConfigurationLoader.builder().buildAndLoadString(reader.lines().collect(Collectors.joining("\n"))).get(WaterfallFileData.class);
    }

    @ConfigSerializable
    public static class WaterfallFileData extends FileData {

        @Setting("depends")
        private List<String> hardDepends;
        @Setting("softDepends")
        private List<String> softDepends;

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

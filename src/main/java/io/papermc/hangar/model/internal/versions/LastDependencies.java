package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.model.api.project.version.PluginDependency;

import java.util.List;

public class LastDependencies {
    public static final LastDependencies EMPTY = new LastDependencies(List.of(), List.of());
    private final List<String> platformDependencies;
    private final List<PluginDependency> pluginDependencies;

    public LastDependencies(List<String> platformDependencies, List<PluginDependency> pluginDependencies) {
        this.platformDependencies = platformDependencies;
        this.pluginDependencies = pluginDependencies;
    }

    public List<String> getPlatformDependencies() {
        return platformDependencies;
    }

    public List<PluginDependency> getPluginDependencies() {
        return pluginDependencies;
    }

    @Override
    public String toString() {
        return "LastDependencies{" +
            "platformDependencies=" + platformDependencies +
            ", pluginDependencies=" + pluginDependencies +
            '}';
    }
}

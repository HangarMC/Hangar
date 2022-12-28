package io.papermc.hangar.model.internal.api.requests.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UpdatePluginDependencies {

    @NotNull
    private final Platform platform;
    private final Map<String, @Valid PluginDependency> pluginDependencies;

    @JsonCreator
    public UpdatePluginDependencies(@NotNull final Platform platform, final Set<@Valid PluginDependency> pluginDependencies) {
        this.platform = platform;
        this.pluginDependencies = pluginDependencies.stream().collect(Collectors.toMap(PluginDependency::getName, Function.identity()));
    }

    public Platform getPlatform() {
        return this.platform;
    }

    public Map<String, PluginDependency> getPluginDependencies() {
        return this.pluginDependencies;
    }

    @Override
    public String toString() {
        return "UpdatePluginDependencies{" +
            "platform=" + this.platform +
            ", pluginDependencies=" + this.pluginDependencies +
            '}';
    }
}

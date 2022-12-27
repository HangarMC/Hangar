package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import java.util.Set;
import java.util.SortedSet;

public record LastDependencies(SortedSet<String> platformDependencies, Set<PluginDependency> pluginDependencies) {
}

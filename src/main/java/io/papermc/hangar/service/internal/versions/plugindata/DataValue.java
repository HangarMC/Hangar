package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public abstract class DataValue {

    private final String key;

    public DataValue(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static class StringDataValue extends DataValue  {

        private final String value;

        public StringDataValue(String key, String value) {
            super(key);
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class StringListDataValue extends DataValue {

        private final List<String> value;

        public StringListDataValue(String key, List<String> value) {
            super(key);
            this.value = value;
        }

        public List<String> getValue() {
            return value;
        }
    }

    public static class DependencyDataValue extends DataValue {

        private final Map<Platform, Set<PluginDependency>> value;

        public DependencyDataValue(String key, Platform platform, Set<PluginDependency> dependencies) {
            super(key);
            this.value = new EnumMap<>(Map.of(platform, dependencies));
        }

        public Map<Platform, Set<PluginDependency>> getValue() {
            return value;
        }
    }

    public static class PlatformDependencyDataValue extends DataValue {

        private final Map<Platform, SortedSet<String>> value;

        public PlatformDependencyDataValue(String key, Platform platform, SortedSet<String> versions) {
            super(key);
            this.value = Map.of(platform, versions);
        }

        public PlatformDependencyDataValue(String key, Map<Platform, SortedSet<String>> map) {
            super(key);
            this.value = map;
        }

        public Map<Platform, SortedSet<String>> getValue() {
            return value;
        }
    }
}

package io.papermc.hangar.service.plugindata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.model.generated.PlatformDependency;
import io.papermc.hangar.model.viewhelpers.VersionDependencies;

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

        private final VersionDependencies value;

        public DependencyDataValue(String key, Platform platform, List<Dependency> dependencies) {
            super(key);
            this.value = new VersionDependencies(Map.of(platform, dependencies));
        }

        public VersionDependencies getValue() {
            return value;
        }
    }

    public static class PlatformDependencyDataValue extends DataValue {

        private final List<PlatformDependency> value;

        public PlatformDependencyDataValue(String key, PlatformDependency value) {
            super(key);
            this.value = new ArrayList<>(List.of(value));
        }

        public List<PlatformDependency> getValue() {
            return value;
        }
    }
}

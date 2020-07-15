package me.minidigger.hangar.service.plugindata;

import java.util.List;
import java.util.UUID;

import me.minidigger.hangar.model.generated.Dependency;

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

        private final List<Dependency> value;

        public DependencyDataValue(String key, List<Dependency> value) {
            super(key);
            this.value = value;
        }

        public List<Dependency> getValue() {
            return value;
        }
    }

    public static class UUIDDataValue extends DataValue {

        private final UUID value;

        public UUIDDataValue(String key, UUID value) {
            super(key);
            this.value = value;
        }

        public UUID getValue() {
            return value;
        }
    }
}

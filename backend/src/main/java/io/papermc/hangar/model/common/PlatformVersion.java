package io.papermc.hangar.model.common;

public record PlatformVersion(String version, String[] subVersions) {

    public boolean contains(final String version) {
        for (final String subVersion : this.subVersions) {
            if (subVersion.equals(version)) {
                return true;
            }
        }
        return false;
    }
}

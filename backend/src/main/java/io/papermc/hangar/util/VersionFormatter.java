package io.papermc.hangar.util;

import java.util.List;

public final class VersionFormatter {

    private VersionFormatter() {
    }

    /**
     * Formats a sorted version range string.
     *
     * @param versions    list of versions to stringify
     * @param allVersions sorted list of all valid versions
     * @return formatted version range string
     * @throws IllegalArgumentException if versions contains a string not included in allVersions
     */
    public static String formatVersionRange(final List<String> versions, final List<String> allVersions) {
        if (versions.isEmpty()) {
            return "";
        } else if (versions.size() == 1) {
            return versions.get(0);
        }

        versions.sort((version1, version2) -> {
            final int index1 = allVersions.indexOf(version1);
            final int index2 = allVersions.indexOf(version2);
            if (index1 == -1) {
                throw new IllegalArgumentException("Version " + version1 + " not included in allVersions");
            } else if (index2 == -1) {
                throw new IllegalArgumentException("Version " + version2 + " not included in allVersions");
            }
            return index1 - index2;
        });

        final StringBuilder builder = new StringBuilder();
        String fromVersion = versions.get(0);
        String lastVersion = fromVersion;
        int lastVersionIndex = allVersions.indexOf(fromVersion);
        for (int i = 1; i < versions.size(); i++) {
            final String version = versions.get(i);
            final int versionIndex = allVersions.indexOf(version);
            if (versionIndex != lastVersionIndex + 1) {
                // Append last version/range if a new range starts
                if (!lastVersion.equals(fromVersion)) {
                    builder.append(fromVersion).append('-').append(lastVersion);
                } else {
                    builder.append(fromVersion);
                }

                builder.append(", ");
                fromVersion = version;
            }

            lastVersion = version;
            lastVersionIndex = versionIndex;
        }

        // Append last version or range
        builder.append(fromVersion);
        if (!fromVersion.equals(lastVersion)) {
            builder.append('-').append(lastVersion);
        }
        return builder.toString();
    }
}

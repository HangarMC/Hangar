package io.papermc.hangar.util;

import java.util.ArrayList;
import java.util.List;

public final class VersionFormatter {

    private VersionFormatter() {
    }

    /**
     * Formats a sorted version range string.
     *
     * @param versions    list of versions to stringify
     * @param allVersions sorted list of all valid versions
     * @return formatted version range strings from oldest to newest
     * @throws IllegalArgumentException if versions contains a string not included in allVersions
     */
    public static List<String> formatVersionRange(final List<String> versions, final List<String> allVersions) {
        if (versions.isEmpty()) {
            return List.of("");
        } else if (versions.size() == 1) {
            return List.of(versions.getFirst());
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

        final List<String> formattedVersions = new ArrayList<>();
        String fromVersion = versions.getFirst();
        String lastVersion = fromVersion;
        int lastVersionIndex = allVersions.indexOf(fromVersion);
        for (int i = 1; i < versions.size(); i++) {
            final String version = versions.get(i);
            final int versionIndex = allVersions.indexOf(version);
            if (versionIndex != lastVersionIndex + 1) {
                // Append last version/range if a new range starts
                if (!lastVersion.equals(fromVersion)) {
                    formattedVersions.add(fromVersion + '-' + lastVersion);
                } else {
                    formattedVersions.add(fromVersion);
                }

                fromVersion = version;
            }

            lastVersion = version;
            lastVersionIndex = versionIndex;
        }

        // Append last version or range
        if (!fromVersion.equals(lastVersion)) {
            formattedVersions.add(fromVersion + '-' + lastVersion);
        } else {
            formattedVersions.add(fromVersion);
        }
        return formattedVersions;
    }

    public static String formatVersionRangeString(final List<String> versions, final List<String> allVersions) {
        return String.join(", ", formatVersionRange(versions, allVersions));
    }
}

package io.papermc.hangar.util;

import io.papermc.hangar.config.hangar.HangarConfig;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {

    private StringUtils() {
    }

    private static HangarConfig hangarConfig;

    static {
        // doesn't work in unit test: but we can ignore that
        try {
            hangarConfig = StaticContextAccessor.getBean(HangarConfig.class);
        } catch (IllegalStateException ex) {
            hangarConfig = null;
        }
    }

    /**
     * Returns a URL readable string from the specified string.
     *
     * @param str String to create slug for
     * @return Slug of string
     */
    public static String slugify(String str) {
        return compact(str).replace(' ', '-');
    }

    /**
     * Returns the specified String with all consecutive spaces removed.
     *
     * @param str String to compact
     * @return Compacted string
     */
    public static String compact(String str) {
        return str.trim().replaceAll(" +", " ");
    }

    /**
     * Returns a version number split into an ordered list of numbers
     *
     * @param str version string to check (e.g. 2.3.1) MUST BE ALL NUMBERS
     * @return the list of integers in ltr order
     */
    public static List<Integer> splitVersionNumber(String str) {
        return Arrays.stream(str.split("\\.")).map(Integer::parseInt).collect(Collectors.toList());
    }

    /**
     * Takes a nullable input and returns itself or if blank, null
     *
     * @param input input string
     * @return itself or null
     */
    public static String stringOrNull(@Nullable String input) {
        if (input == null || input.isBlank()) return null;
        return input;
    }

    /**
     * Formats the avatar url for a name
     *
     * @param name name (user, project)
     * @return the avatar url
     */
    public static String avatarUrl(String name) {
        return String.format(hangarConfig.security.api.getAvatarUrl(), name);
    }

    @NotNull
    @Deprecated(forRemoval = true)
    public static Collection<String> parseKeywords(@Nullable String input) {
        return input == null ? Set.of() : Set.of(input.split("\\s+"));
    }

    @Deprecated(forRemoval = true)
    public static <T extends Throwable> long getVersionId(@NotNull String versionString, T error) throws T {
        int index = versionString.lastIndexOf('.');
        try {
            return Long.parseLong(versionString.substring(index + 1));
        } catch (NumberFormatException ex) {
            throw error;
        }

    }

    private static final Pattern LAST_WHOLE_VERSION = Pattern.compile("((?<=,\\s)|^)[0-9.]{2,}(?=-[0-9.]+$)");
    private static final Pattern PREV_HAS_HYPHEN = Pattern.compile("(?<=\\d-)[0-9.]+$");
    private static final Pattern PREV_HAS_COMMA_OR_FIRST = Pattern.compile("((?<=,\\s)|^)[0-9.]+$");

    /**
     * Format a list of version numbers (will do sorting)
     *
     * @param versionNumbers version numbers
     * @return formatted string
     */
    public static String formatVersionNumbers(List<String> versionNumbers) {
        versionNumbers.sort((version1, version2) -> {
            int vnum1 = 0, vnum2 = 0;

            for (int i = 0, j = 0; (i < version1.length() || j < version2.length()); ) {

                while (i < version1.length() && version1.charAt(i) != '.') {
                    vnum1 = vnum1 * 10 + (version1.charAt(i) - '0');
                    i++;
                }

                while (j < version2.length() && version2.charAt(j) != '.') {
                    vnum2 = vnum2 * 10 + (version2.charAt(j) - '0');
                    j++;
                }
                if (vnum1 > vnum2) {
                    return 1;
                }
                if (vnum2 > vnum1) {
                    return -1;
                }

                vnum1 = vnum2 = 0;
                i++;
                j++;
            }
            return 0;
        });

        return versionNumbers.stream().reduce("", (verString, version) -> {
            if (verString.isBlank()) {
                return version;
            }
            List<Integer> versionArr = StringUtils.splitVersionNumber(version);
            Matcher hyphen = PREV_HAS_HYPHEN.matcher(verString);
            Matcher comma = PREV_HAS_COMMA_OR_FIRST.matcher(verString);
            if (hyphen.find()) {
                String[] group = hyphen.group().split("\\.");
                int prevVersion = Integer.parseInt(group[group.length - 1]);
                Matcher prevVersionMatcher = LAST_WHOLE_VERSION.matcher(verString);
                if (!prevVersionMatcher.find()) {
                    throw new IllegalArgumentException("Bad version string");
                }
                List<Integer> previousWholeVersion = StringUtils.splitVersionNumber(prevVersionMatcher.group());
                if (previousWholeVersion.size() == versionArr.size()) {
                    if (versionArr.get(versionArr.size() - 1) - 1 == prevVersion) {
                        return verString.replaceFirst("-[0-9.]+$", "-" + version);
                    } else {
                        return verString + ", " + version;
                    }
                } else {
                    return verString + ", " + version;
                }
            } else if (comma.find()) {
                List<Integer> prevVersion = StringUtils.splitVersionNumber(comma.group());
                if (prevVersion.size() == versionArr.size()) {
                    if (versionArr.get(versionArr.size() - 1) - 1 == prevVersion.get(prevVersion.size() - 1)) {
                        return verString + "-" + version;
                    } else {
                        return verString + ", " + version;
                    }
                } else {
                    return verString + ", " + version;
                }
            } else {
                throw new IllegalArgumentException("bad formatting: " + version);
            }
        });
    }

    public static String parsePythonNullable(String input) {
        return input.equals("None") ? null : input;
    }

    public static boolean isAnyEqualIgnoreCase(@NotNull String lhs, String @NotNull ... rhs) {
        if (rhs.length == 0) {
            return false;
        }

        for (String string : rhs) {
            if (lhs.equalsIgnoreCase(string)) {
                return true;
            }
        }

        return false;
    }
}

package io.papermc.hangar.util;

public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Returns a URL readable string from the specified string.
     *
     * @param str String to create slug for
     * @return Slug of string
     */
    public static String slugify(final String str) {
        return compact(str).replace(' ', '-');
    }

    /**
     * Returns the specified String with all consecutive spaces removed.
     *
     * @param str String to compact
     * @return Compacted string
     */
    public static String compact(final String str) {
        return str.trim().replaceAll(" +", " ");
    }
}

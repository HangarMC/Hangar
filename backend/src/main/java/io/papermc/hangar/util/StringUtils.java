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

    // https://rosettacode.org/wiki/Longest_common_substring#Java
    public static String lcs(final String a, final String b) {
        if (a.length() > b.length())
            return lcs(b, a);

        String res = "";
        for (int ai = 0; ai < a.length(); ai++) {
            for (int len = a.length() - ai; len > 0; len--) {

                for (int bi = 0; bi <= b.length() - len; bi++) {

                    if (a.regionMatches(ai, b, bi, len) && len > res.length()) {
                        res = a.substring(ai, ai + len);
                    }
                }
            }
        }
        return res;
    }
}

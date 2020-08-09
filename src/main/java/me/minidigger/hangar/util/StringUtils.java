package me.minidigger.hangar.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {

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

}

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

    private static final char[] hexArray = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int j = 0;
        while (j < bytes.length) {
            int v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];

            j++;
        }
        return new String(hexChars);
    }

    public static String md5ToHex(byte[] bytes) {
        try {
            return bytesToHex(MessageDigest.getInstance("MD5").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

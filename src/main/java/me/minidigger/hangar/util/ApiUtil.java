package me.minidigger.hangar.util;

public class ApiUtil {

    public static long limitOrDefault(Long limit, long defaultValue) {
        return Math.min(limit == null ? defaultValue : limit, defaultValue);
    }

    public static long offsetOrZero(Long offset) {
        return Math.max(offset == null ? 0 : offset, 0);
    }
}

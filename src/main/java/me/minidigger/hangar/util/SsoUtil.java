package me.minidigger.hangar.util;

public class SsoUtil {
    public static String parsePythonNullable(String input) {
        return input.equals("None") ? null : input;
    }
}

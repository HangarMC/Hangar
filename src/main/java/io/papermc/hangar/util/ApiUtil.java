package io.papermc.hangar.util;

import io.papermc.hangar.db.model.UsersTable;

public class ApiUtil {

    private ApiUtil() { }

    public static long limitOrDefault(Long limit, long defaultValue) {
        return Math.min(limit == null ? defaultValue : limit, defaultValue);
    }

    public static long offsetOrZero(Long offset) {
        return Math.max(offset == null ? 0 : offset, 0);
    }

    public static Long userIdOrNull(UsersTable usersTable) {
        return usersTable == null ? null : usersTable.getId();
    }
}

package io.papermc.hangar.util;

import io.papermc.hangar.db.model.UsersTable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ApiUtil {

    private ApiUtil() { }

    public static long limitOrDefault(Long limit, long defaultValue) {
        if (limit < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit should be greater than 0");
        return Math.min(limit == null ? defaultValue : limit, defaultValue);
    }

    public static long offsetOrZero(Long offset) {
        return Math.max(offset == null ? 0 : offset, 0);
    }

    public static Long userIdOrNull(UsersTable usersTable) {
        return usersTable == null ? null : usersTable.getId();
    }

    public static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badly formatted date " + date);
        }
    }
}

package io.papermc.hangar.util;

import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.modelold.generated.ProjectSortingStrategy;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ApiUtil {

    private ApiUtil() { }

    @Deprecated
    public static long limitOrDefault(Long limit, long defaultValue) {
        if (limit != null && limit < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit should be greater than 0");
        return Math.min(limit == null ? defaultValue : limit, defaultValue);
    }

    @Deprecated
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

    @Deprecated
    public static ProjectSortingStrategy strategyOrDefault(@Nullable ProjectSortingStrategy strategy) {
        return strategy == null ? ProjectSortingStrategy.Default : strategy;
    }
}

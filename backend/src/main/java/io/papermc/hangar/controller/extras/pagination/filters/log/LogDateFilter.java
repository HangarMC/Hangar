package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogDateFilter.LogDateFilterInstance;
import io.papermc.hangar.exceptions.HangarApiException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class LogDateFilter implements Filter<LogDateFilterInstance, String[]> {

    private static final String FROM = "dateFrom";
    private static final String TO = "dateTo";

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of(FROM, TO);
    }

    @Override
    public String getDescription() {
        return "Filters logs to a date range, both bounds inclusive";
    }

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return new String[]{webRequest.getParameter(FROM), webRequest.getParameter(TO)};
    }

    @Override
    public @NotNull LogDateFilterInstance create(final NativeWebRequest webRequest) {
        final String[] value = this.getValue(webRequest);
        return new LogDateFilterInstance(parseDate(value[0], FROM), parseDate(value[1], TO));
    }

    private static @Nullable LocalDate parseDate(final String value, final String paramName) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (final DateTimeParseException e) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, paramName + " must be an ISO date (yyyy-MM-dd)");
        }
    }

    record LogDateFilterInstance(@Nullable LocalDate from, @Nullable LocalDate to) implements Filter.FilterInstance {

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            if (this.from != null) {
                sb.append(" AND la.created_at::date >= :logDateFrom");
                q.bind("logDateFrom", this.from);
            }
            if (this.to != null) {
                sb.append(" AND la.created_at::date <= :logDateTo");
                q.bind("logDateTo", this.to);
            }
        }
    }
}

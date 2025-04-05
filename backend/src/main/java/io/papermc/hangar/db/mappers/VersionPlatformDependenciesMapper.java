package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.PlatformService;
import io.papermc.hangar.util.VersionFormatter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class VersionPlatformDependenciesMapper implements ColumnMapper<Map<Platform, SortedSet<String>>> {

    private static final String KEY = "platformDependenciesAttribute";

    private final ObjectMapper objectMapper;

    public VersionPlatformDependenciesMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<Platform, SortedSet<String>> map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
        final String raw = r.getString(columnNumber);
        final Map<Platform, SortedSet<String>> result = new EnumMap<>(Platform.class);

        if (raw == null) {
            ctx.define(KEY, result);
            return result;
        }

        try {
            final JsonNode parsedPlatforms = this.objectMapper.readTree(raw);
            for (final JsonNode plat : parsedPlatforms) {
                final Platform platform = Platform.values()[plat.get("platform").asInt()];
                //noinspection unchecked
                SortedSet<String> versions = this.objectMapper.convertValue(plat.get("versions"), SortedSet.class);
                result.put(platform, versions);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ctx.define(KEY, result);
        return result;
    }

    @Component
    public static class VersionPlatformDependenciesFormattedMapper implements ColumnMapper<Map<Platform, List<String>>> {

        private final PlatformService platformService;

        public VersionPlatformDependenciesFormattedMapper(final @Lazy PlatformService platformService) {
            this.platformService = platformService;
        }

        @Override
        public Map<Platform, List<String>> map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
            if (ctx.getAttribute(KEY) instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked") final Map<Platform, Set<String>> platformDependencies = (Map<Platform, Set<String>>) map;
                final Map<Platform, List<String>> result = new EnumMap<>(Platform.class);
                platformDependencies.forEach((platform, value) -> {
                    final List<String> fullVersionsForPlatform = this.platformService.getFullVersionsForPlatform(platform);
                    final List<String> formattedVersionRange = VersionFormatter.formatVersionRange(new ArrayList<>(value), fullVersionsForPlatform);
                    result.put(platform, formattedVersionRange);
                });
                return result;
            }
            return Map.of();
        }
    }
}

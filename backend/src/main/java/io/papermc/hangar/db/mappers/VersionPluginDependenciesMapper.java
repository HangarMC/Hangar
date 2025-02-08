package io.papermc.hangar.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

@Component
public class VersionPluginDependenciesMapper implements ColumnMapper<Map<Platform, Set<PluginDependency>>> {

    private final ObjectMapper objectMapper;

    public VersionPluginDependenciesMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<Platform, Set<PluginDependency>> map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
        final String raw = r.getString(columnNumber);
        final Map<Platform, Set<PluginDependency>> result = new EnumMap<>(Platform.class);

        if (raw == null) {
            return result;
        }

        try {
            final JsonNode parsedDependencies = this.objectMapper.readTree(raw);
            for (final JsonNode download : parsedDependencies) {
                final String name = download.get("name").asText();
                final JsonNode projectId = download.get("project_id");
                final boolean required = download.get("required").asBoolean();
                final String externalUrl = download.get("external_url").asText();
                final Platform platform = Platform.values()[download.get("platform").asInt()];
                final PluginDependency pluginDependency = new PluginDependency(name, projectId instanceof NullNode ? null : projectId.asLong(), required, null, externalUrl, platform);
                result.computeIfAbsent(platform, k -> new TreeSet<>()).add(pluginDependency);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
